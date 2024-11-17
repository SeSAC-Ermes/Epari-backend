package com.example.epari.exam.dto.response;

import com.example.epari.exam.domain.Choice;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
class ChoiceResponseDto {

	private Integer number;

	private String choiceText;

	public static ChoiceResponseDto from(Choice choice) {
		return ChoiceResponseDto.builder()
				.number(choice.getNumber())
				.choiceText(choice.getChoiceText())
				.build();
	}

}
