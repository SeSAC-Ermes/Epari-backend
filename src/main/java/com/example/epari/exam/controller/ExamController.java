package com.example.epari.exam.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.epari.exam.dto.request.ExamRequestDto;
import com.example.epari.exam.service.ExamService;

import lombok.RequiredArgsConstructor;

/**
 * 시험 관련 HTTP 요청을 처리하는 Controller 클래스
 */
@RestController
@RequestMapping("/apu/lecture/{lectureId}/exams")
@RequiredArgsConstructor
public class ExamController {

	private final ExamService examService;

	@PostMapping
	public ResponseEntity<Long> createExam(
			@PathVariable Long lectureId,
			@RequestBody ExamRequestDto examRequestDto) {
		Long examId = examService.createExam(lectureId, examRequestDto);
		return ResponseEntity.ok(examId);
	}

}
