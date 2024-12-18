package com.example.epari.exam.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.epari.exam.dto.request.ExamRequestDto;
import com.example.epari.exam.dto.response.ExamListResponseDto;
import com.example.epari.exam.dto.response.ExamResponseDto;
import com.example.epari.exam.service.ExamService;
import com.example.epari.global.annotation.CurrentUserEmail;
import com.example.epari.global.common.enums.ExamStatus;

import lombok.RequiredArgsConstructor;

/**
 * 시험 관련 HTTP 요청을 처리하는 Controller 클래스
 */
@RestController
@RequestMapping("/api/courses/{courseId}/exams")
@RequiredArgsConstructor
public class ExamController {

	private final ExamService examService;

	// 시험 목록 조회
	@GetMapping
	@PreAuthorize("hasAnyRole('INSTRUCTOR', 'STUDENT')")
	public ResponseEntity<ExamListResponseDto> getExams(
			@PathVariable Long courseId,
			@RequestParam(required = false) ExamStatus status,
			@CurrentUserEmail String email,
			Authentication authentication) {

		String role = authentication.getAuthorities().stream()
				.findFirst()
				.map(GrantedAuthority::getAuthority)
				.orElseThrow(() -> new IllegalStateException("권한 정보를 찾을 수 없습니다."));

		ExamListResponseDto exams = examService.getExams(courseId, status, email, role);
		return ResponseEntity.ok(exams);
	}

	// 시험 상세 정보 조회
	@GetMapping("/{examId}")
	@PreAuthorize("hasAnyRole('INSTRUCTOR', 'STUDENT')")
	public ResponseEntity<ExamResponseDto> getExam(
			@PathVariable Long courseId,
			@PathVariable Long examId,
			@CurrentUserEmail String email,
			Authentication authentication) {
		String role = authentication.getAuthorities().stream()
				.findFirst()
				.map(GrantedAuthority::getAuthority)
				.orElseThrow(() -> new IllegalStateException("권한 정보를 찾을 수 없습니다."));

		ExamResponseDto exam = examService.getExam(courseId, examId, email, role);
		return ResponseEntity.ok(exam);
	}

	// 시험 생성
	@PostMapping
	@PreAuthorize("hasRole('INSTRUCTOR') and @courseSecurityChecker.checkInstructorAccess(#courseId, #instructorEmail)")
	public ResponseEntity<Long> createExam(
			@PathVariable Long courseId,
			@RequestBody ExamRequestDto examRequestDto,
			@CurrentUserEmail String instructorEmail) {
		Long examId = examService.createExam(courseId, examRequestDto, instructorEmail);
		return ResponseEntity.ok(examId);
	}

	// 시험 수정
	@PutMapping("/{examId}")
	@PreAuthorize("hasRole('INSTRUCTOR') and @courseSecurityChecker.checkInstructorAccess(#courseId, #instructorEmail)")
	public ResponseEntity<ExamResponseDto> updateExam(
			@PathVariable Long courseId,
			@PathVariable Long examId,
			@RequestBody ExamRequestDto examRequestDto,
			@CurrentUserEmail String instructorEmail) {
		ExamResponseDto updateExam = examService.updateExam(courseId, examId, examRequestDto, instructorEmail);
		return ResponseEntity.ok(updateExam);
	}

	// 시험 삭제
	@DeleteMapping("/{examId}")
	@PreAuthorize("hasRole('INSTRUCTOR') and @courseSecurityChecker.checkInstructorAccess(#courseId, #instructorEmail)")
	public ResponseEntity<Void> deleteExam(
			@PathVariable Long courseId,
			@PathVariable Long examId,
			@CurrentUserEmail String instructorEmail) {
		examService.deleteExam(courseId, examId, instructorEmail);
		return ResponseEntity.noContent().build();
	}

}
