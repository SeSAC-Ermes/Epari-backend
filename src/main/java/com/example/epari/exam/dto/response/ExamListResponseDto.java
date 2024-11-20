package com.example.epari.exam.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

/**
 * 시험 상태를 나타내는 Dto
 */
@Getter
@Builder
public class ExamListResponseDto {

	private List<ExamSummaryDto> scheduledExams;    // 예정된 시험

	private List<ExamSummaryDto> inProgressExams;   // 진행중인 시험

	private List<ExamSummaryDto> completedExams;    // 완료된 시험

}
