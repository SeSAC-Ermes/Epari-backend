package com.example.epari.exam.dto.common;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

/**
 * 답안 제출 DTO
 */
@Getter
@Setter
public class AnswerSubmissionDto {

	private String answer;

	private LocalDateTime submittedAt;

}
