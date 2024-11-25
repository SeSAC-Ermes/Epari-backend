package com.example.epari.admin.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.epari.admin.dto.InstructorApprovalRequestDTO;
import com.example.epari.admin.dto.StudentApprovalRequestDTO;
import com.example.epari.admin.exception.CourseNotFoundException;
import com.example.epari.admin.repository.AdminCourseStudentRepository;
import com.example.epari.course.domain.Course;
import com.example.epari.course.domain.CourseStudent;
import com.example.epari.course.repository.CourseRepository;
import com.example.epari.user.domain.Instructor;
import com.example.epari.user.domain.Student;
import com.example.epari.user.repository.InstructorRepository;
import com.example.epari.user.repository.StudentRepository;

import lombok.RequiredArgsConstructor;

/**
 * 관리자가 사용자를 관리하는 비즈니스 로직을 처리하는 서비스 클래스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

	private final StudentRepository studentRepository;

	private final CourseRepository courseRepository;

	private final AdminCourseStudentRepository courseStudentRepository;

	private final InstructorRepository instructorRepository;

	/**
	 * 수강생 승인 처리 메서드
	 * 수강생 저장 및 과목 매핑 수행
	 */
	@Transactional
	public String approveStudent(String email, StudentApprovalRequestDTO request) {
		// 1. 사용자 저장
		Student student = Student
				.createStudent(email, request.getName());

		studentRepository.save(student);

		// 2. 과목 매핑
		Course course = courseRepository.findById(request.getCourseId())
				.orElseThrow(CourseNotFoundException::new);

		courseStudentRepository.save(new CourseStudent(course, student));

		return course.getName();
	}

	/**
	 * 강사 승인 처리 메서드
	 */
	@Transactional
	public void approveInstructor(String email, InstructorApprovalRequestDTO request) {
		// 1. 사용자 저장
		instructorRepository.save(Instructor.createInstructor(email, request.getName()));
	}

}
