package com.example.epari.exam.service;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.epari.exam.domain.ExamResult;
import com.example.epari.exam.service.GradingService.ScoreStatistics;

/*
 * 점수 계산 서비스
 */
@Component
public class ScoreCalculator {

	// 평균 점수 계산
	public double calculateAverageScore(List<ExamResult> examResults) {

		if (examResults == null || examResults.isEmpty()) {
			return 0.0;
		}

		return examResults.stream()
				.mapToDouble(ExamResult::getEarnedScore)
				.average()
				.orElse(0.0);
	}

	// 최고/최저 점수 계산
	public ScoreStatistics calculateStatistics(List<ExamResult> examResults) {

		if (examResults == null || examResults.isEmpty()) {
			return new ScoreStatistics(0, 0);
		}

		int maxScore = examResults.stream()
				.mapToInt(ExamResult::getEarnedScore)
				.max()
				.orElse(0);

		int minScore = examResults.stream()
				.mapToInt(ExamResult::getEarnedScore)
				.min()
				.orElse(0);

		return new ScoreStatistics(maxScore, minScore);
	}

}
