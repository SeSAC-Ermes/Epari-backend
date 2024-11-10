package com.example.epari.exam.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.epari.exam.dto.request.ExamRequestDto;
import com.example.epari.exam.dto.response.ExamResponseDto;
import com.example.epari.exam.service.ExamService;

import lombok.RequiredArgsConstructor;

/**
 * 시험 관련 HTTP 요청을 처리하는 Controller 클래스
 */
@RestController
@RequestMapping("/api/lectures/{lectureId}/exams")
@RequiredArgsConstructor
public class ExamController {

	private final ExamService examService;

	// 시험 생성
	@PostMapping
	public ResponseEntity<Long> createExam(
			@PathVariable Long lectureId,
			@RequestBody ExamRequestDto examRequestDto) {
		Long examId = examService.createExam(lectureId, examRequestDto);
		return ResponseEntity.ok(examId);
	}

	// 특정 강의에 해당하는 시험 정보 조회
	@GetMapping
	public ResponseEntity<List<ExamResponseDto>> getExams(@PathVariable Long lectureId) {
		List<ExamResponseDto> exams = examService.getExamsByLecture(lectureId);
		return ResponseEntity.ok(exams);
	}

	// 특정 강의에 속한 시험 상세 조회
	@GetMapping("/{examId}")
	public ResponseEntity<ExamResponseDto> getExam(
			@PathVariable Long lectureId,
			@PathVariable("examId") Long id) {
		ExamResponseDto exam = examService.getExam(lectureId, id);
		return ResponseEntity.ok(exam);
	}

}
