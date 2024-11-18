package com.example.epari.exam.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChoiceRequestDto {

	@NotNull(message = "보기 번호는 필수입니다")
	@Min(value = 1, message = "보기 번호는 1 이상이어야 합니다")
	private int number;

	@NotNull(message = "보기 내용은 필수입니다")
	private String choiceText;

}
