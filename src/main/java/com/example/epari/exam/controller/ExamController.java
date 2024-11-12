package com.example.epari.exam.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.epari.exam.dto.request.ExamRequestDto;
import com.example.epari.exam.dto.response.ExamResponseDto;
import com.example.epari.exam.service.ExamService;
import com.example.epari.exam.service.InstructorExamService;
import com.example.epari.global.annotation.CurrentUserEmail;

import lombok.RequiredArgsConstructor;

/**
 * 시험 관련 HTTP 요청을 처리하는 Controller 클래스
 */
@RestController
@RequestMapping("/api/courses/{courseId}/exams")
@RequiredArgsConstructor
public class ExamController {

	private final ExamService examService;

	private final InstructorExamService instructorExamService;

	// 시험 생성
	@PostMapping
	public ResponseEntity<Long> createExam(
			@PathVariable Long courseId,
			@RequestBody ExamRequestDto examRequestDto,
			@CurrentUserEmail String instructorEmail) {
		Long examId = instructorExamService.createExam(courseId, examRequestDto, instructorEmail);
		return ResponseEntity.ok(examId);
	}

	// 특정 강의에 해당하는 시험 정보 조회
	@GetMapping
	public ResponseEntity<List<ExamResponseDto>> getExams(@PathVariable Long courseId) {
		List<ExamResponseDto> exams = examService.getExamByCourse(courseId);
		return ResponseEntity.ok(exams);
	}

	// 특정 강의에 속한 시험 상세 조회
	@GetMapping("/{examId}")
	public ResponseEntity<ExamResponseDto> getExam(
			@PathVariable Long courseId,
			@PathVariable("examId") Long id) {
		ExamResponseDto exam = examService.getExam(courseId, id);
		return ResponseEntity.ok(exam);
	}

	// 특정 강의에 속한 시험 수정
	@PutMapping("/{examId}")
	public ResponseEntity<ExamResponseDto> updateExam(
			@PathVariable Long courseId,
			@PathVariable("examId") Long id,
			@RequestBody ExamRequestDto examRequestDto,
			@CurrentUserEmail String instructorEmail) {
		ExamResponseDto updateExam = instructorExamService.updateExam(courseId, id, examRequestDto, instructorEmail);
		return ResponseEntity.ok(updateExam);
	}

	// 특정 강의에 속한 시험 삭제
	@DeleteMapping("/{examId}")
	public ResponseEntity<Void> deleteExam(
			@PathVariable Long courseId,
			@PathVariable("examId") Long id,
			@CurrentUserEmail String instructorEmail) {
		instructorExamService.deleteExam(courseId, id, instructorEmail);
		return ResponseEntity.noContent().build();
	}

}
