package com.example.epari.exam.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.epari.course.domain.CourseStudent;
import com.example.epari.course.repository.CourseStudentRepository;
import com.example.epari.exam.domain.Exam;
import com.example.epari.exam.domain.ExamResult;
import com.example.epari.exam.domain.ExamScore;
import com.example.epari.exam.repository.ExamResultRepository;
import com.example.epari.global.common.enums.ExamStatus;
import com.example.epari.global.validator.ExamGradingValidator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * 시험 채점 서비스를 담당하는 클래스
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ExamGradingService {

	private final ExamResultRepository examResultRepository;

	private final CourseStudentRepository courseStudentRepository;

	private final ExamGradingValidator examGradingValidator;

	// 시험 결과 채점 및 미제출자 처리
	public void processGrading(ExamResult examResult) {
		examGradingValidator.validateExamResultForGrading(examResult.getId());
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
				int earnedScore = gradeAnswer(score);
				score.updateScore(earnedScore);
			}

			examResult.updateScore();
			examResultRepository.save(examResult);
		}
	}

	// 시험 결과의 총점 정합성 검증
	@Transactional(readOnly = true)
	public boolean verifyTotalScore(ExamResult examResult) {
		int calculatedTotal = examResult.getScores().stream()
				.mapToInt(ExamScore::getEarnedScore)
				.sum();
		return calculatedTotal == examResult.getEarnedScore();
	}

	// 개별 시험 결과 채점
	public void gradeExamResult(Long examResultId) {
		ExamResult examResult = examGradingValidator.validateExamResultForGrading(examResultId);

		// 각 문제별 채점
		for (ExamScore score : examResult.getScores()) {
			int earnedScore = gradeAnswer(score);
			score.updateScore(earnedScore);
		}

		// 채점 결과 반영
		examResult.updateScore();
		examResultRepository.save(examResult);
		log.info("Exam graded - resultId: {}, totalScore: {}", examResult.getId(), examResult.getEarnedScore());
	}

	// 개별 문제 답안 채점
	private int gradeAnswer(ExamScore score) {
		String studentAnswer = score.getStudentAnswer();
		if (score.getQuestion().validateAnswer(studentAnswer)) {
			return score.getQuestion().getScore();
		}
		return 0;
	}

	// 시험 점수 통계 정보
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
