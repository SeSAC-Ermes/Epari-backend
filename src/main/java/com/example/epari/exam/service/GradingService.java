package com.example.epari.exam.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.epari.exam.domain.ExamQuestion;
import com.example.epari.exam.domain.ExamResult;
import com.example.epari.exam.domain.ExamScore;
import com.example.epari.exam.repository.ExamResultRepository;
import com.example.epari.global.common.enums.ExamStatus;
import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class GradingService {

	private final ExamResultRepository examResultRepository;

	/**
     * 단순 채점 처리 (내부용)
     */
	public void processGrading(ExamResult examResult) {
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

	/**
	 * 개별 시험 결과 채점 (API용)
	 */
	public void gradeExamResult(Long examResultId) {
		ExamResult examResult = examResultRepository.findById(examResultId)
				.orElseThrow(() -> new BusinessBaseException(ErrorCode.EXAM_RESULT_NOT_FOUND));

		if (examResult.getStatus() != ExamStatus.SUBMITTED) {
			throw new BusinessBaseException(ErrorCode.EXAM_NOT_SUBMITTED);
		}

		// 각 문제별 채점
		for (ExamScore score : examResult.getScores()) {
			int earnedScore = gradeAnswer(score);
			score.updateScore(earnedScore);
		}

		// 채점 결과 반영
		examResult.updateScore();
		examResultRepository.save(examResult);

		log.info("Exam graded - resultId: {}, totalScore: {}", examResultId, examResult.getTotalScore());
	}

	/**
	 * 평균 점수 계산
	 */
	@Transactional(readOnly = true)
	public double calculateAverageScore(Long examId) {
		List<ExamResult> gradedResults = examResultRepository.findByExamIdAndStatus(examId, ExamStatus.GRADED);

		if (gradedResults.isEmpty()) {
			return 0.0;
		}

		int totalScore = gradedResults.stream()
				.mapToInt(ExamResult::getTotalScore)
				.sum();

		return (double)totalScore / gradedResults.size();
	}

	/**
	 * 총점에서 최고/최저 점수 정보 조회
	 */
	@Transactional(readOnly = true)
	public ScoreStatistics calculateScoreStatistics(Long examId) {
		List<ExamResult> gradedResults = examResultRepository.findByExamIdAndStatus(examId, ExamStatus.GRADED);

		if (gradedResults.isEmpty()) {
			return new ScoreStatistics(0, 0);
		}

		int maxScore = gradedResults.stream()
				.mapToInt(ExamResult::getTotalScore)
				.max()
				.orElse(0);

		int minScore = gradedResults.stream()
				.mapToInt(ExamResult::getTotalScore)
				.min()
				.orElse(0);

		return new ScoreStatistics(maxScore, minScore);
	}

	private int gradeAnswer(ExamScore score) {
		String studentAnswer = score.getStudentAnswer();
		if (score.getQuestion().validateAnswer(studentAnswer)) {
			return score.getQuestion().getScore();
		}
		return 0;
	}

	@Getter
	public static class ScoreStatistics {

		private final int maxScore;

		private final int minScore;

		public ScoreStatistics(int maxScore, int minScore) {
			this.maxScore = maxScore;
			this.minScore = minScore;
		}

	}

}
