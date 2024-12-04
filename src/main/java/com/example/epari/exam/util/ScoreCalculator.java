package com.example.epari.exam.util;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.epari.course.repository.CourseStudentRepository;
import com.example.epari.exam.domain.ExamResult;
import com.example.epari.exam.dto.common.ExamStatistics;
import com.example.epari.exam.repository.ExamResultRepository;
import com.example.epari.exam.service.GradingService;
import com.example.epari.exam.service.GradingService.ScoreStatistics;
import com.example.epari.global.common.enums.ExamStatus;

/*
 * 점수 계산 서비스
 */
@Component
public class ScoreCalculator {

	private final CourseStudentRepository courseStudentRepository;
	private final ExamResultRepository examResultRepository;
	
	// 생성자 주입
	public ScoreCalculator(
        CourseStudentRepository courseStudentRepository,
        ExamResultRepository examResultRepository
    ) {
        this.courseStudentRepository = courseStudentRepository;
        this.examResultRepository = examResultRepository;
    }

    // 시험의 평균 점수 계산
    public double calculateExamAverageScore(Long examId) {
        List<ExamResult> gradedResults = examResultRepository
            .findByExamIdAndStatus(examId, ExamStatus.GRADED);
        return calculateAverageScore(gradedResults);
    }

	// 시험의 최고/최저 점수 통계 계산
    public ScoreStatistics calculateExamStatistics(Long examId) {
        List<ExamResult> gradedResults = examResultRepository
            .findByExamIdAndStatus(examId, ExamStatus.GRADED);
        return calculateStatistics(gradedResults);
    }

	// 특정 학생 한 명의 평균 점수 계산
	public double calculateAverageScore(List<ExamResult> examResults) {

		if (examResults == null || examResults.isEmpty()) {
			return 0.0;
		}

		return examResults.stream()
				.mapToDouble(ExamResult::getEarnedScore)
				.average()
				.orElse(0.0);
	}

	// 특정 시험의 최고/최저 점수 계산
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

	// 특정 시험의 통계 계산
    public ExamStatistics calculateExamStatistics(List<ExamResult> examResults, Long courseId) {
        if (examResults == null || examResults.isEmpty()) {
            return new ExamStatistics(0, 0, 0.0); // 기본값 반환
        }

        int totalStudentCount = courseStudentRepository.countByCourseId(courseId);
        int submittedStudentCount = (int) examResults.stream()
                .filter(r -> r.getStatus() == ExamStatus.SUBMITTED || r.getStatus() == ExamStatus.COMPLETED)
                .count();
        double averageScore = calculateAverageScore(examResults);

        return ExamStatistics.builder()
                .totalStudentCount(totalStudentCount)
                .submittedStudentCount(submittedStudentCount)
                .averageScore(averageScore)
                .build();
    }

}
