package com.example.epari.exam.dto.common;

import java.time.LocalDateTime;

import com.example.epari.global.common.enums.ExamStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExamResultSummaryDto {

	private String studentName;

	private String studentEmail;

	private ExamStatus status;

	private Integer totalScore;

	private LocalDateTime submittedAt;

}
