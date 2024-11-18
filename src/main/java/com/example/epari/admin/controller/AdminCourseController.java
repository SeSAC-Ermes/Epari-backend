package com.example.epari.admin.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.epari.admin.dto.CourseSearchResponseDTO;
import com.example.epari.admin.service.AdminCourseService;

import lombok.RequiredArgsConstructor;

/**
 * 관리자를 위한 REST API 컨트롤러
 * 강의 관리
 */
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

}
