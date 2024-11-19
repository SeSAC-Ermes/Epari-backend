package com.example.epari.global.config.security.checker;

import org.springframework.stereotype.Component;

import com.example.epari.course.repository.CourseRepository;

import lombok.RequiredArgsConstructor;

@Component("courseSecurityChecker")
@RequiredArgsConstructor
public class CourseSecurityChecker {

	private final CourseRepository courseRepository;

	public boolean checkInstructorAccess(Long courseId, String email) {
		return courseRepository.existsByCourseIdAndInstructorEmail(courseId, email);
	}

	public boolean checkStudentAccess(Long courseId, String email) {
		return courseRepository.existsByCourseIdAndStudentEmail(courseId, email);
	}

}
