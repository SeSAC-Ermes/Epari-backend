package com.example.epari.exam.dto.common;

import java.time.LocalDateTime;

import com.example.epari.global.common.enums.ExamStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExamSubmissionStatusDto {

	private ExamStatus status;

	private LocalDateTime startTime;

	private LocalDateTime endTime;

	private Integer submittedQuestionCount;

	private Integer totalQuestionCount;

	private LocalDateTime examEndTime;     // 시험 종료 시각

	private Integer remainingTimeInMinutes; // 남은 시간

}
