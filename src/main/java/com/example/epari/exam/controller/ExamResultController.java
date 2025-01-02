package com.example.epari.exam.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.epari.exam.dto.common.ExamResultDetailDto;
import com.example.epari.exam.dto.common.ExamResultSummaryDto;
import com.example.epari.exam.dto.response.ExamResultResponseDto;
import com.example.epari.exam.service.ExamResultService;
import com.example.epari.global.annotation.CurrentUserEmail;

import lombok.RequiredArgsConstructor;

/**
 * 학생의 시험 결과를 조회하는 REST API 컨트롤러 구현
 */
@RestController
@RequestMapping("/api/courses/{courseId}/exams")
@RequiredArgsConstructor
public class ExamResultController {

	private final ExamResultService examResultService;

	// 강의 전체 성적 조회 (강사)
	@GetMapping("/scores")
	@PreAuthorize("hasRole('INSTRUCTOR') and @courseSecurityChecker.checkInstructorAccess(#courseId, #email)")
	public ResponseEntity<List<ExamResultResponseDto>> getCourseExamResults(
			@PathVariable Long courseId,
			@CurrentUserEmail String email) {

		List<ExamResultResponseDto> response = examResultService.getCourseExamResults(courseId, email);
		return ResponseEntity.ok(response);
	}

	// 시험별 결과 목록 조회 (강사)
	@GetMapping("/{examId}/results")
	@PreAuthorize("hasRole('INSTRUCTOR') and @courseSecurityChecker.checkInstructorAccess(#courseId, #email)")
	public ResponseEntity<List<ExamResultSummaryDto>> getExamResults(
			@PathVariable Long courseId,
			@PathVariable Long examId,
			@CurrentUserEmail String email) {
		List<ExamResultSummaryDto> results = examResultService.getExamResults(courseId, examId, email);
		return ResponseEntity.ok(results);
	}

	// 시험 결과 상세 조회 (강사/학생)
	@GetMapping("/{examId}/results/{resultId}")
	@PreAuthorize("hasAnyRole('INSTRUCTOR', 'STUDENT')")
	public ResponseEntity<ExamResultDetailDto> getExamResult(
			@PathVariable Long courseId,
			@PathVariable Long examId,
			@PathVariable Long resultId,
			@CurrentUserEmail String email,
			Authentication authentication) {
		String role = authentication.getAuthorities().stream()
				.findFirst()
				.map(GrantedAuthority::getAuthority)
				.orElseThrow(() -> new IllegalStateException("권한 정보를 찾을 수 없습니다."));

		ExamResultDetailDto result = examResultService.getStudentExamResultById(
				courseId, resultId, email, role);
		return ResponseEntity.ok(result);
	}

}
