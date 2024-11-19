package com.example.epari.exam.dto.common;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnswerSubmissionDto {

	private String answer;

	private LocalDateTime submittedAt;

}
