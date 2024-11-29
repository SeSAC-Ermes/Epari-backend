package com.example.epari.exam.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.epari.course.domain.CourseStudent;
import com.example.epari.course.repository.CourseStudentRepository;
import com.example.epari.exam.domain.Exam;
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

	private final CourseStudentRepository courseStudentRepository;

	private final ScoreCalculator scoreCalculator;

	// 단순 채점
	public void processGrading(ExamResult examResult) {
		Exam exam = examResult.getExam();

		// 1. 미제출자 처리
		List<CourseStudent> courseStudents = courseStudentRepository.findAllCourseStudentsByCourseId(
				exam.getCourse().getId());
		List<Long> submittedStudentIds = examResultRepository.findByExamId(exam.getId())
				.stream()
				.map(result -> result.getStudent().getId())
				.collect(Collectors.toList());

		// 미제출자들의 ExamResult 생성
		for (CourseStudent courseStudent : courseStudents) {
			if (!submittedStudentIds.contains(courseStudent.getStudent().getId())) {
				ExamResult notSubmittedResult = ExamResult.builder()
						.exam(exam)
						.student(courseStudent.getStudent())
						.status(ExamStatus.NOT_SUBMITTED)
						.submitTime(LocalDateTime.now())
						.build();
				examResultRepository.save(notSubmittedResult);
				log.info("Created NOT_SUBMITTED result for student: {}", courseStudent.getId());
			}
		}

		// 2. 기존 채점 로직
		if (examResult.getStatus() == ExamStatus.SUBMITTED) {
			for (ExamScore score : examResult.getScores()) {
				ExamQuestion question = score.getQuestion();
				String studentAnswer = score.getStudentAnswer();

				boolean isCorrect = question.validateAnswer(studentAnswer);
				int earnedScore = isCorrect ? question.getScore() : 0;
				score.updateScore(earnedScore);
			}

			examResult.updateScore();
			examResultRepository.save(examResult);
		}
	}

	@Transactional(readOnly = true)
	public boolean verifyTotalScore(ExamResult examResult) {
		int calculatedTotal = examResult.getScores().stream().mapToInt(ExamScore::getEarnedScore).sum();

		return calculatedTotal == examResult.getEarnedScore();
	}

	// 개별 시험 결과 채점 (API용)
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

	// 평균 점수 계산
	@Transactional(readOnly = true)
	public double calculateAverageScore(Long examId) {
		List<ExamResult> gradedResults = examResultRepository.findByExamIdAndStatus(examId, ExamStatus.GRADED);
		return scoreCalculator.calculateAverageScore(gradedResults);
	}

	// 총점에서 최고/최저 점수 정보 조회
	@Transactional(readOnly = true)
	public ScoreStatistics calculateScoreStatistics(Long examId) {
		List<ExamResult> gradedResults = examResultRepository.findByExamIdAndStatus(examId, ExamStatus.GRADED);
		return scoreCalculator.calculateStatistics(gradedResults);
	}

	// 개별 문제 채점
	private int gradeAnswer(ExamScore score) {
		String studentAnswer = score.getStudentAnswer();
		if (score.getQuestion().validateAnswer(studentAnswer)) {
			return score.getQuestion().getScore();
		}
		return 0;
	}

	// 최고/최저 점수 정보 클래스
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
