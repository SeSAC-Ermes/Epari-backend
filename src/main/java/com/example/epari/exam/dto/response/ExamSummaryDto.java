package com.example.epari.exam.dto.response;

import java.time.LocalDateTime;

import com.example.epari.exam.domain.Exam;
import com.example.epari.exam.domain.ExamResult;
import com.example.epari.exam.dto.common.ExamStatistics;
import com.example.epari.global.common.enums.ExamStatus;

import lombok.Builder;
import lombok.Getter;

/**
 * 시험 정보를 반환하는 Dto
 */
@Getter
@Builder
public class ExamSummaryDto {

	private Long id;

	private String title;

	private LocalDateTime examDateTime;

	private Integer duration;

	private Integer totalScore;

	// 강사용 추가 정보
	private Integer totalStudentCount;      // 총 수강생 수

	private Integer submittedStudentCount;  // 제출한 학생 수

	private Double averageScore;            // 평균 점수

	// 학생용 추가 정보
	private ExamStatus status;              // 학생별 시험 상태

	private Integer earnedScore;            // 취득 점수

	private LocalDateTime submittedAt;      // 제출 시간

	// 강사용 DTO 생성
	public static ExamSummaryDto forInstructor(Exam exam, ExamStatistics statistics) {
		return ExamSummaryDto.builder()
				.id(exam.getId())
				.title(exam.getTitle())
				.examDateTime(exam.getExamDateTime())
				.duration(exam.getDuration())
				.totalScore(exam.getTotalScore())
				.totalStudentCount(statistics.getTotalStudentCount())
				.submittedStudentCount(statistics.getSubmittedStudentCount())
				.averageScore(statistics.getAverageScore())
				.build();
	}

	// 학생용 DTO 생성
	public static ExamSummaryDto forStudent(Exam exam, ExamResult result) {
		return ExamSummaryDto.builder()
				.id(exam.getId())
				.title(exam.getTitle())
				.examDateTime(exam.getExamDateTime())
				.duration(exam.getDuration())
				.totalScore(exam.getTotalScore())
				.status(result != null ? result.getStatus() : null)
				.earnedScore(result != null ? result.getEarnedScore() : null)
				.submittedAt(result != null ? result.getSubmitTime() : null)
				.build();
	}

}
