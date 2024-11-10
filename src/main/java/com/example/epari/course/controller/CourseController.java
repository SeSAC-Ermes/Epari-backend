package com.example.epari.course.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.epari.course.dto.course.CourseRequestDto;
import com.example.epari.course.dto.course.CourseResponseDto;
import com.example.epari.course.service.CourseService;
import com.example.epari.global.annotation.CurrentUserEmail;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@EnableMethodSecurity
public class CourseController {

	private final CourseService courseService;

	/**
	 * 강의 생성
	 * 강의 id로 조회
	 */
	@PostMapping
	@PreAuthorize("hasRole('INSTRUCTOR')")
	public ResponseEntity<CourseResponseDto> createCourse(
			@RequestParam Long instructorId,
			@RequestBody CourseRequestDto request) {
		return ResponseEntity.ok(courseService.createCourse(instructorId, request));
	}

	/**
	 * 강의 id로 조회 상세 페이지 이동 시
	 */
	@GetMapping("/{id}")
	public ResponseEntity<CourseResponseDto> getCourse(@PathVariable Long id) {
		return ResponseEntity.ok(courseService.getCourse(id));
	}

	/**
	 * 사용자 역할에 따른 강의 목록 조회
	 */
	@GetMapping("/usercourses")
	public ResponseEntity<List<CourseResponseDto>> getMyCourses(
			@CurrentUserEmail String email) {

		List<CourseResponseDto> courses = courseService.getMyCourses(email);
		return ResponseEntity.ok(courses);
	}

	/**
	 * 강의 id로 수정
	 * 강사 id로 검증
	 */
	@PutMapping("/{id}")
	@PreAuthorize("hasRole('INSTRUCTOR')")
	public ResponseEntity<CourseResponseDto> updateCourse(
			@PathVariable Long id,
			@RequestParam Long instructorId,
			@RequestBody CourseRequestDto request
	) {
		return ResponseEntity.ok(courseService.updateCourse(id, instructorId, request));
	}

	/**
	 * 강의 id로 삭제
	 * 강사 id로 검증
	 */
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('INSTRUCTOR')")
	public ResponseEntity<Void> deleteCourse(
			@PathVariable Long id,
			@RequestParam Long instructorId
	) {
		courseService.deleteCourse(id, instructorId);
		return ResponseEntity.ok().build();
	}

}
