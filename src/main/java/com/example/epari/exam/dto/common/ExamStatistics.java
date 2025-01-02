package com.example.epari.exam.dto.common;

import lombok.Builder;
import lombok.Getter;

/**
 * 시험 제출 통계 정보를 위한 DTO
 */
@Getter
@Builder
public class ExamStatistics {

	private Integer totalStudentCount;

	private Integer submittedStudentCount;

	private Double averageScore;

	public ExamStatistics(int totalStudentCount, int submittedStudentCount, double averageScore) {
        this.totalStudentCount = totalStudentCount;
        this.submittedStudentCount = submittedStudentCount;
        this.averageScore = averageScore;
    }

}
