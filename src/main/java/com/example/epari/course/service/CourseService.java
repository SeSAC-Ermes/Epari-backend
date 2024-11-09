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
import com.example.epari.user.domain.Instructor;
import com.example.epari.user.repository.InstructorRepository;

import lombok.RequiredArgsConstructor;

/**
 * 강의 관련 비즈니스 로직을 처리하는 Service 클래스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
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
				.orElseThrow(() -> new IllegalArgumentException("강사를 찾을 수 없습니다. ID: " + instructorId));

		Course course = Course.createCourse(
				request.getName(),
				request.getStartDate(),
				request.getEndDate(),
				request.getClassroom(),
				instructor
		);
		return CourseResponseDto.from(courseRepository.save(course));
	}

	/**
	 * 강의 정보를 조회합니다.
	 */
	public CourseResponseDto getCourse(Long courseId) {
		Course course = courseRepository.findById(courseId)
				.orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다. ID: " + courseId));
		return CourseResponseDto.from(course);
	}

	/**
	 * 학생이 수강 중인 강의 목록을 조회합니다.
	 * 강사가 담당하는 강의 목록을 조회합니다.
	 */
	public List<CourseResponseDto> getMyCourses(String email) {
		BaseUser user = baseUserRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

		if (user.getRole() == UserRole.INSTRUCTOR) {
			return courseRepository.findAllByInstructorId(user.getId()).stream()
					.map(CourseResponseDto::from)
					.collect(Collectors.toList());
		} else {
			return courseRepository.findAllByStudentId(user.getId()).stream()
					.map(CourseResponseDto::from)
					.collect(Collectors.toList());
		}
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
				.orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다. ID: " + courseId));

		if (!course.getInstructor().getId().equals(instructorId)) {
			throw new IllegalArgumentException("해당 강의에 대한 권한이 없습니다.");
		}

		course.updateCourse(
				request.getName(),
				request.getStartDate(),
				request.getEndDate(),
				request.getClassroom()
		);
		return CourseResponseDto.from(course);
	}

	/**
	 * 강의를 삭제합니다.
	 */
	@Transactional
	public void deleteCourse(Long courseId, Long instructorId) {
		Course course = courseRepository.findById(courseId)
				.orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다. ID: " + courseId));

		if (!course.getInstructor().getId().equals(instructorId)) {
			throw new IllegalArgumentException("해당 강의에 대한 권한이 없습니다.");
		}

		courseRepository.deleteById(courseId);
	}

}
