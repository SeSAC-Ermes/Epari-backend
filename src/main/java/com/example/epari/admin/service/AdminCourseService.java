package com.example.epari.admin.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.epari.admin.dto.AdminCourseListResponseDto;
import com.example.epari.admin.dto.AdminCourseRequestDto;
import com.example.epari.admin.dto.CourseSearchResponseDTO;
import com.example.epari.admin.repository.AdminCourseRepository;
import com.example.epari.course.domain.Course;
import com.example.epari.course.domain.Curriculum;
import com.example.epari.course.repository.CurriculumRepository;
import com.example.epari.global.common.service.S3FileService;
import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;
import com.example.epari.user.domain.Instructor;
import com.example.epari.user.repository.InstructorRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 관리자가 강의를 관리하는 비즈니스 로직을 처리하는 서비스 클래스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCourseService {

	private static final String COURSE_IMAGE_DIRECTORY = "course-images";

	private final AdminCourseRepository courseRepository;

	private final InstructorRepository instructorRepository;

	private final CurriculumRepository curriculumRepository;

	private final S3FileService s3FileService;

	/**
	 * 키워드 기반 강의 검색 메서드
	 */
	public List<CourseSearchResponseDTO> searchCourses(String keyword) {
		return courseRepository.searchCoursesWithDTO(keyword);
	}

	/**
	 * 강의 생성
	 */
	@Transactional
	@CacheEvict(value = "courses", key = "'all'")
	public Long createCourse(AdminCourseRequestDto request) {
		// 1. 강사 존재 여부 확인
		Instructor instructor = instructorRepository.findById(request.getInstructorId())
				.orElseThrow(() -> new BusinessBaseException(ErrorCode.INSTRUCTOR_NOT_FOUND));

		// 2. 강의 생성
		Course course = Course.builder()
				.name(request.getName())
				.startDate(request.getStartDate())
				.endDate(request.getEndDate())
				.classroom(request.getClassroom())
				.instructor(instructor)
				.build();

		// 3. 이미지 처리
		if (request.getCourseImage() != null && !request.getCourseImage().isEmpty()) {
			String imageUrl = s3FileService.uploadFile(COURSE_IMAGE_DIRECTORY, request.getCourseImage());
			course.updateCourseImage(imageUrl);
		}

		Course savedCourse = courseRepository.save(course);

		// 4. 커리큘럼 생성 및 저장
		List<Curriculum> curriculums = new ArrayList<>();

		for (AdminCourseRequestDto.CurriculumInfo info : request.getCurriculums()) {
			Curriculum curriculum = Curriculum.builder()
					.date(info.getDate())
					.topic(info.getTopic())
					.description(info.getDescription())
					.course(savedCourse)
					.build();
			curriculums.add(curriculum);
		}

		curriculumRepository.saveAll(curriculums);

		return savedCourse.getId();
	}

	/**
	 * 모든 강의 목록 조회
	 */
	@Cacheable(value = "courses", key = "'all'")
	public List<AdminCourseListResponseDto> getCourses() {
		log.info("Fetching all courses with student count");
		List<AdminCourseListResponseDto> courses = courseRepository.findAllWithStudentCount();
		log.info("Found {} courses", courses.size());
		return courses;
	}

}
