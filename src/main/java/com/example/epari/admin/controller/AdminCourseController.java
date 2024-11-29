package com.example.epari.admin.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.epari.admin.dto.AdminCourseDetailResponseDto;
import com.example.epari.admin.dto.AdminCourseListResponseDto;
import com.example.epari.admin.dto.AdminCourseRequestDto;
import com.example.epari.admin.dto.AdminCourseUpdateRequestDto;
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

	/**
	 * 강의 상세 정보 조회
	 */
	@GetMapping("/{courseId}")
	public ResponseEntity<AdminCourseDetailResponseDto> getCourseDetail(@PathVariable Long courseId) {
		log.info("Course detail request received - courseId: {}", courseId);

		AdminCourseDetailResponseDto courseDetail = adminCourseService.getCourseDetail(courseId);

		log.info("Course detail retrieved successfully - courseId: {}", courseId);

		return ResponseEntity.ok(courseDetail);
	}

	/**
	 * 강의 수정
	 */
	@PutMapping("/{courseId}")
	public ResponseEntity<Void> updateCourse(
			@PathVariable Long courseId,
			@Validated @ModelAttribute AdminCourseUpdateRequestDto request) {

		if (!request.isValidDateRange()) {
			return ResponseEntity.badRequest().build();
		}

		if (!request.isAllCurriculumDatesValid()) {
			return ResponseEntity.badRequest().build();
		}

		log.info("Course update request received - courseId: {}, name: {}, instructor: {}",
				courseId, request.getName(), request.getInstructorId());

		adminCourseService.updateCourse(courseId, request);

		log.info("Course updated successfully - courseId: {}", courseId);

		return ResponseEntity.ok().build();
	}

}
