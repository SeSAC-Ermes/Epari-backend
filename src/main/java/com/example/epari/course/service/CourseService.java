package com.example.epari.course.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.epari.course.domain.Course;
import com.example.epari.course.dto.course.CourseRequestDto;
import com.example.epari.course.dto.course.CourseResponseDto;
import com.example.epari.course.repository.CourseRepository;
import com.example.epari.global.common.base.BaseUser;
import com.example.epari.global.common.enums.UserRole;
import com.example.epari.global.common.repository.BaseUserRepository;
import com.example.epari.global.exception.auth.AuthUserNotFoundException;
import com.example.epari.global.exception.auth.InstructorNotFoundException;
import com.example.epari.global.exception.course.CourseInstructorMismatchException;
import com.example.epari.global.exception.course.CourseNotFoundException;
import com.example.epari.user.domain.Instructor;
import com.example.epari.user.repository.InstructorRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 강의 관련 비즈니스 로직을 처리하는 Service 클래스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CourseService {

	private final CourseRepository courseRepository;

	private final InstructorRepository instructorRepository;

	private final BaseUserRepository baseUserRepository;

	/**
	 * 새로운 강의를 생성합니다.
	 */
	@Transactional
	public CourseResponseDto createCourse(Long instructorId, CourseRequestDto request) {
		Instructor instructor = instructorRepository.findById(instructorId)
				.orElseThrow(InstructorNotFoundException::new);

		Course course = Course.createCourse(
				request.getName(),
				request.getStartDate(),
				request.getEndDate(),
				request.getClassroom(),
				instructor
		);

		log.info("Creating new course: {} for instructor: {}", request.getName(), instructorId);
		return CourseResponseDto.from(courseRepository.save(course));
	}

	/**
	 * 강의 정보를 조회합니다.
	 */
	public CourseResponseDto getCourse(Long courseId) {
		Course course = courseRepository.findByIdWithInstructor(courseId)
				.orElseThrow(CourseNotFoundException::new);
		return CourseResponseDto.from(course);
	}

	/**
	 * 학생이 수강 중인 강의 목록을 조회합니다.
	 * 강사가 담당하는 강의 목록을 조회합니다.
	 */
	public List<CourseResponseDto> getMyCourses(String email) {
		BaseUser user = baseUserRepository.findByEmail(email)
				.orElseThrow(AuthUserNotFoundException::new);

		List<Course> courses;
		if (user.getRole() == UserRole.INSTRUCTOR) {
			courses = courseRepository.findAllByInstructorId(user.getId());
		} else {
			courses = courseRepository.findAllByStudentId(user.getId());
		}

		return courses.stream()
				.map(CourseResponseDto::from)
				.collect(Collectors.toList());
	}

	/**
	 * 강사가 담당하는 강의 목록을 조회합니다.
	 */
	public List<CourseResponseDto> getInstructorCourses(Long instructorId) {
		return courseRepository.findAllByInstructorId(instructorId).stream()
				.map(CourseResponseDto::from)
				.collect(Collectors.toList());
	}

	/**
	 * 강의 정보를 수정합니다.
	 */
	@Transactional
	public CourseResponseDto updateCourse(Long courseId, Long instructorId, CourseRequestDto request) {
		Course course = courseRepository.findById(courseId)
				.orElseThrow(CourseNotFoundException::new);

		if (!course.getInstructor().getId().equals(instructorId)) {
			log.warn("Instructor {} attempted to update unauthorized course {}", instructorId, courseId);
			throw new CourseInstructorMismatchException();
		}

		course.updateCourse(
				request.getName(),
				request.getStartDate(),
				request.getEndDate(),
				request.getClassroom()
		);

		log.info("Updated course: {} by instructor: {}", courseId, instructorId);
		return CourseResponseDto.from(course);
	}

	/**
	 * 강의를 삭제합니다.
	 */
	@Transactional
	public void deleteCourse(Long courseId, Long instructorId) {
		Course course = courseRepository.findById(courseId)
				.orElseThrow(CourseNotFoundException::new);

		if (!course.getInstructor().getId().equals(instructorId)) {
			log.warn("Instructor {} attempted to delete unauthorized course {}", instructorId, courseId);
			throw new CourseInstructorMismatchException();
		}

		log.info("Deleting course: {} by instructor: {}", courseId, instructorId);
		courseRepository.deleteById(courseId);
	}

}
