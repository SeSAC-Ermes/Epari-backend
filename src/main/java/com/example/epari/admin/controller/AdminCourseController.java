package com.example.epari.admin.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.epari.admin.dto.AdminCourseListResponseDto;
import com.example.epari.admin.dto.AdminCourseRequestDto;
import com.example.epari.admin.dto.CourseSearchResponseDTO;
import com.example.epari.admin.service.AdminCourseService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 관리자를 위한 REST API 컨트롤러
 * 강의 관리
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/courses")
@RequiredArgsConstructor
public class AdminCourseController {

	private final AdminCourseService adminCourseService;

	/**
	 * keyword 기반 강의 목록 검색 엔드포인트
	 */
	@GetMapping("/search")
	public ResponseEntity<List<CourseSearchResponseDTO>> searchCourses(
			@RequestParam(required = false) String keyword) {
		return ResponseEntity.ok(adminCourseService.searchCourses(keyword));
	}

	/**
	 * 강의 생성
	 */
	@PostMapping
	public ResponseEntity<Long> createCourse(
			@Validated @ModelAttribute AdminCourseRequestDto request) {
		if (!request.isValidDateRange()) {
			return ResponseEntity.badRequest().build();
		}

		log.info("Course creation request received - name: {}, instructor: {}",
				request.getName(), request.getInstructorId());

		Long savedCourseId = adminCourseService.createCourse(request);

		log.info("Course created successfully - id: {}", savedCourseId);

		return ResponseEntity.ok(savedCourseId);
	}

	/**
	 * 모든 강의 목록 조회
	 */
	@GetMapping
	public ResponseEntity<List<AdminCourseListResponseDto>> getCourses() {
		return ResponseEntity.ok(adminCourseService.getCourses());
	}

}
