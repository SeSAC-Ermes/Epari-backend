package com.example.epari.exam.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.epari.exam.dto.common.AnswerSubmissionDto;
import com.example.epari.exam.dto.common.ExamResultDetailDto;
import com.example.epari.exam.dto.common.ExamResultSummaryDto;
import com.example.epari.exam.dto.common.ExamSubmissionStatusDto;
import com.example.epari.exam.service.ExamSubmissionService;
import com.example.epari.global.annotation.CurrentUserEmail;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/courses/{courseId}/exams/{examId}/submission")
@RequiredArgsConstructor
public class ExamSubmissionController {

	private final ExamSubmissionService examSubmissionService;

	// 시험 시작
	@PostMapping("/start")
	@PreAuthorize("hasRole('STUDENT') and @courseSecurityChecker.checkStudentAccess(#courseId, #studentEmail)")
	public ResponseEntity<ExamSubmissionStatusDto> startExam(
			@PathVariable Long courseId,
			@PathVariable Long examId,
			@CurrentUserEmail String studentEmail) {
		ExamSubmissionStatusDto status = examSubmissionService.startExam(courseId, examId, studentEmail);
		return ResponseEntity.ok(status);
	}

	// 답안 임시 저장
	@PostMapping("/questions/{questionId}/save")
	@PreAuthorize("hasRole('STUDENT') and @courseSecurityChecker.checkStudentAccess(#courseId, #studentEmail)")
	public ResponseEntity<Void> saveAnswerTemporarily(
			@PathVariable Long courseId,
			@PathVariable Long examId,
			@PathVariable Long questionId,
			@RequestBody AnswerSubmissionDto answerDto,
			@CurrentUserEmail String studentEmail) {
		examSubmissionService.saveAnswerTemporarily(courseId, examId, questionId, answerDto, studentEmail);
		return ResponseEntity.ok().build();
	}

	// 답안 제출
	@PostMapping("/questions/{questionId}")
	@PreAuthorize("hasRole('STUDENT') and @courseSecurityChecker.checkStudentAccess(#courseId, #studentEmail)")
	public ResponseEntity<Void> submitAnswer(
			@PathVariable Long courseId,
			@PathVariable Long examId,
			@PathVariable Long questionId,
			@RequestBody AnswerSubmissionDto answerDto,
			@CurrentUserEmail String studentEmail) {
		examSubmissionService.submitAnswer(courseId, examId, questionId, answerDto, studentEmail);
		return ResponseEntity.ok().build();
	}

	// 시험 최종 제출
	@PostMapping("/finish")
	@PreAuthorize("hasRole('STUDENT') and @courseSecurityChecker.checkStudentAccess(#courseId, #studentEmail)")
	public ResponseEntity<Void> finishExam(
			@PathVariable Long courseId,
			@PathVariable Long examId,
			@RequestParam(defaultValue = "false") boolean force,
			@CurrentUserEmail String studentEmail) {
		examSubmissionService.finishExam(courseId, examId, studentEmail, force);
		return ResponseEntity.ok().build();
	}

	// 현재 진행 상태 조회
	@GetMapping("/status")
	@PreAuthorize("hasAnyRole('INSTRUCTOR', 'STUDENT')")
	public ResponseEntity<ExamSubmissionStatusDto> getSubmissionStatus(
			@PathVariable Long courseId,
			@PathVariable Long examId,
			@CurrentUserEmail String email) {

		ExamSubmissionStatusDto status = examSubmissionService.getSubmissionStatus(courseId, examId, email);
		return ResponseEntity.ok(status);
	}

	// 학생 결과
	@GetMapping("/result")
	@PreAuthorize("hasRole('STUDENT') and @courseSecurityChecker.checkStudentAccess(#courseId, #studentEmail)")
	public ResponseEntity<ExamResultDetailDto> getStudentExamResult(
			@PathVariable Long courseId,
			@PathVariable Long examId,
			@CurrentUserEmail String studentEmail) {

		ExamResultDetailDto result = examSubmissionService.getStudentExamResult(courseId, examId, studentEmail);
		return ResponseEntity.ok(result);
	}

	// 시험 결과
	@GetMapping("/results")
	@PreAuthorize("hasRole('INSTRUCTOR') and @courseSecurityChecker.checkInstructorAccess(#courseId, #email)")
	public ResponseEntity<List<ExamResultSummaryDto>> getExamResults(
			@PathVariable Long courseId,
			@PathVariable Long examId,
			@CurrentUserEmail String email) {

		List<ExamResultSummaryDto> results = examSubmissionService.getExamResults(courseId, examId);
		return ResponseEntity.ok(results);
	}

}
