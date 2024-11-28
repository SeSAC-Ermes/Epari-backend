package com.example.epari.exam.dto.common;

import java.time.LocalDateTime;

import com.example.epari.global.common.enums.ExamStatus;

import lombok.Builder;
import lombok.Getter;

/**
 * 시험 제출 상태 DTO
 */
@Getter
@Builder
public class ExamSubmissionStatusDto {

	private Long examId;

	private ExamStatus status;

	private Integer submittedQuestionCount;

	private Integer totalQuestionCount;

	private Integer remainingTimeInMinutes;

	private LocalDateTime submitTime;

}
