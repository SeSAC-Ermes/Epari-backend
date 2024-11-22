package com.example.epari.admin.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.epari.admin.dto.InstructorSearchResponseDTO;
import com.example.epari.admin.service.AdminInstructorService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 관리자를 위한 REST API 컨트롤러
 * 강사 관리
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/instructors")
@RequiredArgsConstructor
public class AdminInstructorController {

	private final AdminInstructorService adminInstructorService;

	/**
	 * 이메일 기반 강사 검색 엔드포인트
	 */
	@GetMapping("/search")
	public ResponseEntity<List<InstructorSearchResponseDTO>> searchInstructors(
			@RequestParam(required = false) String email) {
		log.info("Instructor search request received with email: {}", email);

		return ResponseEntity.ok(adminInstructorService.searchInstructors(email));
	}

}
