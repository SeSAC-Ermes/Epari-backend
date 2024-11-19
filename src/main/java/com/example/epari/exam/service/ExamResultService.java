package com.example.epari.exam.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.epari.exam.domain.ExamResult;
import com.example.epari.exam.domain.ExamScore;
import com.example.epari.exam.dto.response.ExamResultResponseDto;
import com.example.epari.exam.repository.ExamResultRepository;
import com.example.epari.global.exception.exam.ExamResultNotFoundException;
import com.example.epari.global.validator.CourseAccessValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 시험 결과 관련 비즈니스 로직을 처리하는 서비스 클래스
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ExamResultService {

	private final ExamResultRepository examResultRepository;

	private final CourseAccessValidator courseAccessValidator;

	/**
	 * 학생의 전체 시험 결과를 조회합니다.
	 */
	public ExamResultResponseDto getStudentExamResults(Long studentId, String email) {

		// 시험 결과 조회
		List<ExamResult> examResults = examResultRepository.findAllByStudentId(studentId);
		if (examResults.isEmpty()) {
			log.warn("No exam results found for student ID: {}", studentId);
			throw new ExamResultNotFoundException();
		}

		ExamResultResponseDto.StudentInfo studentInfo =
				ExamResultResponseDto.StudentInfo.from(examResults.get(0).getStudent());

		List<ExamResultResponseDto.ExamInfo> examInfos = examResults.stream()
				.map(result -> {
					int earnedScore = calculateEarnedScore(result);
					return ExamResultResponseDto.ExamInfo.from(result, earnedScore);
				})
				.collect(Collectors.toList());

		double averageScore = calculateAverageScore(examResults);

		return ExamResultResponseDto.builder()
				.student(studentInfo)
				.examResults(examInfos)
				.averageScore(averageScore)
				.build();
	}

	private int calculateEarnedScore(ExamResult examResult) {
		return examResult.getScores().stream()
				.mapToInt(ExamScore::getEarnedScore)
				.sum();
	}

	private double calculateAverageScore(List<ExamResult> examResults) {
		return examResults.stream()
				.mapToDouble(this::calculateEarnedScore)
				.average()
				.orElse(0.0);
	}

}
