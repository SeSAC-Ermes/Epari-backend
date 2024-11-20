package com.example.epari.exam.dto.common;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuestionResultDto {

	private Long questionId;

	private String questionTitle;

	private String studentAnswer;

	private Integer score;

}
