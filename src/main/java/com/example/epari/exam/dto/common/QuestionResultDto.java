package com.example.epari.exam.dto.common;

import java.util.List;

import com.example.epari.global.common.enums.ExamQuestionType;

import lombok.Builder;
import lombok.Getter;

/**
 * 문제 결과 DTO
 */
@Getter
@Builder
public class QuestionResultDto {

	private Long questionId;

	private String questionTitle;

	private String questionText;

	private List<String> options;

	private String correctAnswer;

	private String studentAnswer;

	private Integer selectedOptionIndex;

	private Integer score;

	private Integer earnedScore;

	private ExamQuestionType type;

}
