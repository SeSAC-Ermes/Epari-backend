package com.example.epari.exam.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.epari.exam.dto.response.ExamResultResponseDto;
import com.example.epari.exam.service.ExamResultService;
import com.example.epari.global.annotation.CurrentUserEmail;

import lombok.RequiredArgsConstructor;

/**
 * 학생의 시험 결과를 조회하는 REST API 컨트롤러 구현
 */

@RestController
@RequestMapping("/api/scores")
@RequiredArgsConstructor
public class ExamResultController {

	private final ExamResultService examResultService;

	@GetMapping("/students/{studentId}")
	@PreAuthorize("hasRole('INSTRUCTOR') or @courseSecurityChecker.checkStudentAccess(#studentId, #email)")
	public ResponseEntity<ExamResultResponseDto> getStudentExamResults(
			@PathVariable Long studentId,
			@CurrentUserEmail String email) {

		ExamResultResponseDto response = examResultService.getStudentExamResults(studentId, email);
		return ResponseEntity.ok(response);
	}

}
