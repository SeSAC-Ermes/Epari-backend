package com.example.epari.exam.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.epari.exam.dto.response.ExamResponseDto;
import com.example.epari.exam.service.StudentExamService;
import com.example.epari.global.annotation.CurrentUserEmail;

import lombok.RequiredArgsConstructor;

/**
 * 학생 시험 관련 HTTP 요청을 처리하는 Controller 클래스
 */
@RestController
@RequestMapping("/api/student/exams")
@RequiredArgsConstructor
public class StudentExamController {

	private final StudentExamService studentExamService;

	// 학생이 수강중인 강의의 모든 시험 조회
	@GetMapping
	public ResponseEntity<List<ExamResponseDto>> getExamsByStudent(@CurrentUserEmail String email) {
		List<ExamResponseDto> exams = studentExamService.getExams(email);
		return ResponseEntity.ok(exams);
	}

}
