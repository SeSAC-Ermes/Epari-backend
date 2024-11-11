package com.example.epari.exam.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.epari.exam.domain.Exam;
import com.example.epari.exam.dto.response.ExamResponseDto;
import com.example.epari.exam.repository.ExamRepository;

import lombok.RequiredArgsConstructor;

/**
 * 학생의 시험 조회 관련 비즈니스 로직을 처리하는 Service 클래스
 */
@Service
@RequiredArgsConstructor
@Transactional
public class StudentExamService {

	private final ExamRepository examRepository;

	public List<ExamResponseDto> getExams(String email) {
		List<Exam> exams = examRepository.findByStudentEmail(email);
		return exams.stream()
				.map(ExamResponseDto::fromExam)
				.collect(Collectors.toList());
	}

}
