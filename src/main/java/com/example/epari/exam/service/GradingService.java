package com.example.epari.exam.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.epari.exam.domain.ExamQuestion;
import com.example.epari.exam.domain.ExamResult;
import com.example.epari.exam.domain.ExamScore;
import com.example.epari.exam.repository.ExamResultRepository;
import com.example.epari.global.common.enums.ExamStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class GradingService {

	private final ExamResultRepository examResultRepository;

	public void gradeExamResult(ExamResult examResult) {
		if (examResult.getStatus() != ExamStatus.SUBMITTED) {
			throw new IllegalStateException("제출된 시험만 채점할 수 있습니다.");
		}

		int totalScore = 0;

		for (ExamScore score : examResult.getScores()) {
			ExamQuestion question = score.getQuestion();
			String studentAnswer = score.getStudentAnswer();

			// 답안 채점
			boolean isCorrect = question.validateAnswer(studentAnswer);
			int earnedScore = isCorrect ? question.getScore() : 0;
			score.updateScore(earnedScore);

			totalScore += earnedScore;
		}

		examResult.updateStatus(ExamStatus.GRADED);
		examResultRepository.save(examResult);
	}

	@Transactional(readOnly = true)
	public boolean verifyTotalScore(ExamResult examResult) {
		int calculatedTotal = examResult.getScores().stream()
				.mapToInt(ExamScore::getEarnedScore)
				.sum();

		return calculatedTotal == examResult.getEarnedScore();
	}

}
