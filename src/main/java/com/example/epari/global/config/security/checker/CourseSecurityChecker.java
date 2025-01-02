package com.example.epari.global.config.security.checker;

import org.springframework.stereotype.Component;

import com.example.epari.course.repository.CourseRepository;
import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;
import com.example.epari.user.domain.Instructor;
import com.example.epari.user.domain.Student;
import com.example.epari.user.repository.InstructorRepository;
import com.example.epari.user.repository.StudentRepository;

import lombok.RequiredArgsConstructor;

@Component("courseSecurityChecker")
@RequiredArgsConstructor
public class CourseSecurityChecker {

	private final CourseRepository courseRepository;

	private final InstructorRepository instructorRepository;

	private final StudentRepository studentRepository;

	public boolean checkInstructorAccess(Long courseId, String email) {
		Instructor instructor = instructorRepository.findByEmail(email)
				.orElseThrow(() -> new BusinessBaseException(ErrorCode.INSTRUCTOR_NOT_FOUND));
		return courseRepository.existsByCourseIdAndInstructorId(courseId, instructor.getId());
	}

	public boolean checkStudentAccess(Long courseId, String email) {
		Student student = studentRepository.findByEmail(email)
				.orElseThrow(() -> new BusinessBaseException(ErrorCode.STUDENT_NOT_FOUND));
		return courseRepository.existsByCourseIdAndStudentId(courseId, student.getId());
	}

}
