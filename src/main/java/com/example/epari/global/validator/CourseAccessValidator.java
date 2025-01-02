package com.example.epari.global.validator;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.epari.course.domain.Course;
import com.example.epari.course.repository.CourseRepository;
import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;
import com.example.epari.global.exception.course.CourseAccessDeniedException;
import com.example.epari.user.domain.Instructor;
import com.example.epari.user.domain.Student;
import com.example.epari.user.repository.InstructorRepository;
import com.example.epari.user.repository.StudentRepository;

import lombok.RequiredArgsConstructor;

/**
 * 접근 권한을 검증하는 클래스
 */
@Component
@RequiredArgsConstructor
public class CourseAccessValidator {

	private final CourseRepository courseRepository;

	private final InstructorRepository instructorRepository;

	private final StudentRepository studentRepository;

	// 강사의 강의 접근권한 검증
	public void validateInstructorAccess(Long courseId, Long instructorId) {
		List<Course> instructorCourses = courseRepository.findAllByInstructorId(instructorId);
		if (instructorCourses.stream().noneMatch(course -> course.getId().equals(courseId))) {
			throw new CourseAccessDeniedException();
		}
	}

	// 학생의 강의 접근권한 검증
	public void validateStudentAccess(Long courseId, Long studentId) {
		List<Course> studentCourses = courseRepository.findAllByStudentId(studentId);
		if (studentCourses.stream().noneMatch(course -> course.getId().equals(courseId))) {
			throw new CourseAccessDeniedException();
		}
	}

	// 강사 이메일 검증
	public Instructor validateInstructorEmail(String email) {
		return instructorRepository.findByEmail(email)
				.orElseThrow(() -> new BusinessBaseException(ErrorCode.INSTRUCTOR_NOT_FOUND));
	}

	// 학생 이메일 검증
	public Student validateStudentEmail(String email) {
		return studentRepository.findByEmail(email)
				.orElseThrow(() -> new BusinessBaseException(ErrorCode.STUDENT_NOT_FOUND));
	}

}
