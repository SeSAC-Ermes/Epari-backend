package com.example.epari.global.validator;

import org.springframework.stereotype.Component;

import com.example.epari.course.repository.CourseRepository;
import com.example.epari.global.exception.course.CourseAccessDeniedException;

import lombok.RequiredArgsConstructor;

/**
 * 접근 권한을 검증하는 클래스
 */
@Component
@RequiredArgsConstructor
public class CourseAccessValidator {

	private final CourseRepository courseRepository;

	// 강사의 강의 접근권한 검증
	public void validateInstructorAccess(Long courseId, String instructorEmail) {
		if (!courseRepository.existsByCourseIdAndInstructorEmail(courseId, instructorEmail)) {
			throw new CourseAccessDeniedException();
		}
	}

	// 학생의 강의 접근권한 검증
	public void validateStudentAccess(Long courseId, String studentEmail) {
		if (!courseRepository.existsByCourseIdAndStudentEmail(courseId, studentEmail)) {
			throw new CourseAccessDeniedException();
		}
	}

}
