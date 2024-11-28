package com.example.epari.exam.dto.common;

import java.time.LocalDateTime;
import java.util.List;

import com.example.epari.global.common.enums.ExamStatus;

import lombok.Builder;
import lombok.Getter;

/**
 * 시험 결과 상세 DTO
 */
@Getter
@Builder
public class ExamResultDetailDto {

	private Long examId;

	private String examTitle;

	private LocalDateTime startTime;

	private LocalDateTime endTime;

	private ExamStatus status;

	private Integer totalScore;

	private List<QuestionResultDto> questionResults;

}
