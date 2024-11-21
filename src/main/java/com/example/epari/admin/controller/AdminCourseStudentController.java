package com.example.epari.admin.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.epari.admin.dto.AvailableStudentResponseDTO;
import com.example.epari.admin.dto.CourseStudentResponseDTO;
import com.example.epari.admin.dto.CourseStudentUpdateRequestDTO;
import com.example.epari.admin.service.AdminCourseStudentService;

import lombok.RequiredArgsConstructor;

/**
 * 관리자를 위한 REST API 컨트롤러
 * 강의와 학생 관련 엔드포인트 관리
 */
@RestController
@RequestMapping("/api/admin/courses/{courseId}/students")
@RequiredArgsConstructor
public class AdminCourseStudentController {

	private final AdminCourseStudentService courseStudentService;

	/**
	 * 강의에 등록된 수강생 목록 조회
	 */
	@GetMapping
	public ResponseEntity<List<CourseStudentResponseDTO>> getEnrolledStudents(
			@PathVariable Long courseId) {
		return ResponseEntity.ok(courseStudentService.getEnrolledStudents(courseId));
	}

	/**
	 * 강의에 등록 가능한 수강생 목록 조회
	 */
	@GetMapping("/available")
	public ResponseEntity<List<AvailableStudentResponseDTO>> getAvailableStudents(
			@PathVariable Long courseId,
			@RequestParam(required = false) String keyword) {
		return ResponseEntity.ok(courseStudentService.getAvailableStudents(courseId, keyword));
	}

	/**
	 * 수강생 목록 업데이트
	 */
	@PutMapping
	public ResponseEntity<Void> updateEnrolledStudents(
			@PathVariable Long courseId,
			@RequestBody CourseStudentUpdateRequestDTO request) {
		courseStudentService.updateEnrolledStudents(courseId, request);

		return ResponseEntity.ok().build();
	}

}
