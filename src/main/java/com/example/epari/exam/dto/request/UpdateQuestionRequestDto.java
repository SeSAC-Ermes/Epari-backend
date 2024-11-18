package com.example.epari.exam.dto.request;

import java.util.List;

import com.example.epari.global.common.enums.ExamQuestionType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateQuestionRequestDto {

	@NotBlank(message = "문제 내용은 필수입니다")
	private String questionText;

	@Min(value = 1, message = "배점은 1점 이상이어야 합니다")
	private int score;

	@NotNull(message = "문제 유형은 필수입니다")
	private ExamQuestionType type;

	@NotBlank(message = "정답은 필수입니다")
	private String correctAnswer;

	// 객관식일 경우
	private List<ChoiceRequestDto> choices;

	// 유효성 검사 메소드
	public void validate() {
		if (ExamQuestionType.MULTIPLE_CHOICE.equals(type)) {
			// 객관식 문제 유효성 검사
			if (choices == null || choices.isEmpty()) {
				throw new IllegalArgumentException("객관식 문제는 보기가 필요합니다");
			}

			// 객관식 정답 번호 유효성 검사
			try {
				int answerNumber = Integer.parseInt(correctAnswer);
				if (answerNumber < 1 || answerNumber > choices.size()) {
					throw new IllegalArgumentException("올바르지 않은 정답 번호입니다");
				}
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("객관식 문제의 정답은 숫자여야 합니다");
			}
		}
	}

}
