package com.example.epari.exam.dto.common;

import java.util.List;

import com.example.epari.global.common.enums.ExamQuestionType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuestionResultDto {

	private Long questionId;

	private String questionTitle;

	private String questionText;  // 문제 내용

	private List<String> options;  // 보기 목록

	private String correctAnswer;  // 정답

	private String studentAnswer;  // 학생 답안

	private Integer selectedOptionIndex;  // 학생이 선택한 보기 번호

	private Integer score;  // 배점

	private Integer earnedScore;  // 획득 점수

	private ExamQuestionType type;

}
