package com.example.epari.admin.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.epari.admin.dto.AdminCourseDetailResponseDto;
import com.example.epari.admin.dto.AdminCourseListResponseDto;
import com.example.epari.admin.dto.AdminCourseRequestDto;
import com.example.epari.admin.dto.AdminCourseUpdateRequestDto;
import com.example.epari.admin.dto.CourseSearchResponseDTO;
import com.example.epari.admin.dto.CurriculumDetailDto;
import com.example.epari.admin.exception.CourseNotFoundException;
import com.example.epari.admin.repository.AdminCourseRepository;
import com.example.epari.admin.repository.AdminCurriculumRepository;
import com.example.epari.course.domain.Course;
import com.example.epari.course.domain.Curriculum;
import com.example.epari.global.common.service.S3FileService;
import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;
import com.example.epari.global.exception.auth.InstructorNotFoundException;
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

	private final AdminCurriculumRepository curriculumRepository;

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

	/**
	 * 강의 상세 정보 조회
	 */
	public AdminCourseDetailResponseDto getCourseDetail(Long courseId) {
		// 1. courseId로 Course 엔티티 조회
		Course course = courseRepository.findByIdWithInstructorAndStudents(courseId)
				.orElseThrow(() -> new BusinessBaseException(ErrorCode.COURSE_NOT_FOUND));

		// 2. 커리큘럼 정보 조회
		List<CurriculumDetailDto> curriculums = courseRepository.findCurriculumsByCourseId(courseId);

		// 3. 엔티티와 커리큘럼 정보로 DTO를 한 번만 생성
		return AdminCourseDetailResponseDto.of(
				course.getId(),
				course.getName(),
				course.getClassroom(),
				course.getInstructor().getId(),
				course.getInstructor().getName(),
				course.getStartDate(),
				course.getEndDate(),
				course.getImageUrl(),
				course.getCourseStudents().size(),
				curriculums
		);
	}

	/**
	 * 강의 정보 수정
	 * 기본 정보, 강사 정보, 커리큘럼, 이미지를 수정합니다.
	 */
	@Transactional
	@Caching(evict = {
			@CacheEvict(value = "courses", key = "'all'"),
			@CacheEvict(value = "curriculums", key = "#courseId")
	})
	public void updateCourse(Long courseId, AdminCourseUpdateRequestDto request) {
		// 1. 강의 및 연관 데이터 조회
		Course course = courseRepository.findByIdWithInstructorAndStudents(courseId)
				.orElseThrow(CourseNotFoundException::new);

		// 2. 강사 조회
		Instructor instructor = instructorRepository.findById(request.getInstructorId())
				.orElseThrow(InstructorNotFoundException::new);

		// 3. 이미지 처리
		updateCourseImage(course, request);

		// 4. 강의 기본 정보 수정
		course.updateCourse(
				request.getName(),
				request.getStartDate(),
				request.getEndDate(),
				request.getClassroom(),
				instructor
		);

		// 5. 커리큘럼 수정
		updateCourseCurriculums(course, request.getCurriculums());
	}

	/**
	 * 강의 이미지 수정
	 */
	private void updateCourseImage(Course course, AdminCourseUpdateRequestDto request) {
		// 기존 이미지 삭제 요청이 있는 경우
		if (request.isRemoveExistingImage() && course.getImageUrl() != null) {
			s3FileService.deleteFile(course.getImageUrl());
			course.updateCourseImage(null);
		}

		// 새로운 이미지가 있는 경우
		if (request.getCourseImage() != null && !request.getCourseImage().isEmpty()) {
			// 기존 이미지가 있다면 삭제
			if (course.getImageUrl() != null) {
				s3FileService.deleteFile(course.getImageUrl());
			}
			String imageUrl = s3FileService.uploadFile(COURSE_IMAGE_DIRECTORY, request.getCourseImage());
			course.updateCourseImage(imageUrl);
		}
	}

	/**
	 * 커리큘럼 수정
	 */
	private void updateCourseCurriculums(Course course,
			List<AdminCourseUpdateRequestDto.CurriculumUpdateInfo> curriculumInfos) {
		Map<Long, Curriculum> existingCurriculumsMap = curriculumRepository.findByCourseId(course.getId())
				.stream()
				.collect(Collectors.toMap(Curriculum::getId, c -> c));

		List<Long> curriculumsToDelete = new ArrayList<>();
		List<Curriculum> curriculumsToAdd = new ArrayList<>();

		for (AdminCourseUpdateRequestDto.CurriculumUpdateInfo info : curriculumInfos) {
			if (info.isDeleted() && info.getId() != null) {
				// 삭제할 커리큘럼
				curriculumsToDelete.add(info.getId());
			} else if (info.getId() == null) {
				// 새로운 커리큘럼
				curriculumsToAdd.add(Curriculum.builder()
						.date(info.getDate())
						.topic(info.getTopic())
						.description(info.getDescription())
						.course(course)
						.build());
			} else {
				// 기존 커리큘럼 수정
				Curriculum existingCurriculum = existingCurriculumsMap.get(info.getId());
				if (existingCurriculum != null) {
					existingCurriculum.update(  // Curriculum 엔티티에 update 메서드 추가 필요
							info.getDate(),
							info.getTopic(),
							info.getDescription()
					);
				}
			}
		}

		// 커리큘럼 삭제 및 추가 처리
		if (!curriculumsToDelete.isEmpty()) {
			curriculumRepository.deleteByIdInAndCourseId(curriculumsToDelete, course.getId());
		}
		if (!curriculumsToAdd.isEmpty()) {
			curriculumRepository.saveAll(curriculumsToAdd);
		}
	}

}
