package com.example.epari.exam.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.epari.exam.dto.request.CreateQuestionRequestDto;
import com.example.epari.exam.dto.request.UpdateQuestionRequestDto;
import com.example.epari.exam.dto.response.ExamQuestionResponseDto;
import com.example.epari.exam.service.ExamQuestionService;
import com.example.epari.global.annotation.CurrentUserEmail;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 시험 문제 관련 HTTP 요청을 처리하는 Controller 클래스
 */
@RestController
@RequestMapping("/api/courses/{courseId}/exams/{examId}/questions")
@RequiredArgsConstructor
public class ExamQuestionController {

	private final ExamQuestionService examQuestionService;

	// 시험 문제 생성
	@PostMapping
	@PreAuthorize("hasRole('INSTRUCTOR')")
	public ResponseEntity<Long> createQuestion(
			@PathVariable Long courseId,
			@PathVariable Long examId,
			@RequestBody @Valid CreateQuestionRequestDto request,
			@CurrentUserEmail String instructorEmail) {
		Long questionId = examQuestionService.addQuestion(courseId, examId, request, instructorEmail);
		return ResponseEntity.ok(questionId);
	}

	// 시험 문제 재정렬
	@PutMapping("/{questionId}/order")
	@PreAuthorize("hasRole('INSTRUCTOR')")
	public ResponseEntity<Void> reorderQuestion(
			@PathVariable Long courseId,
			@PathVariable Long examId,
			@PathVariable Long questionId,
			@RequestParam int newNumber,
			@CurrentUserEmail String instructorEmail) {
		examQuestionService.reorderQuestions(courseId, examId, questionId, newNumber, instructorEmail);
		return ResponseEntity.ok().build();
	}

	// 시험 문제 수정
	@PutMapping("/{questionId}")
	public ResponseEntity<ExamQuestionResponseDto> updateQuestion(
			@PathVariable Long courseId,
			@PathVariable Long examId,
			@PathVariable Long questionId,
			@RequestBody @Valid UpdateQuestionRequestDto requestDto,
			@CurrentUserEmail String instructorEmail) {

		ExamQuestionResponseDto updatedQuestion = examQuestionService.updateQuestion(
				courseId, examId, questionId, requestDto, instructorEmail);
		return ResponseEntity.ok(updatedQuestion);
	}

	// 시험 문제 삭제
	@DeleteMapping("/{questionId}")
	public ResponseEntity<Void> deleteQuestion(
			@PathVariable Long courseId,
			@PathVariable Long examId,
			@PathVariable Long questionId,
			@CurrentUserEmail String instructorEmail) {

		examQuestionService.deleteQuestion(courseId, examId, questionId, instructorEmail);
		return ResponseEntity.noContent().build();
	}

}
