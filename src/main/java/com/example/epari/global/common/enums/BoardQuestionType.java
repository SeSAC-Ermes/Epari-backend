package com.example.epari.global.common.enums;

import lombok.Getter;

@Getter
public enum BoardQuestionType {
	PUBLIC("공개"),
	PRIVATE("비공개");

	private final String description;

	BoardQuestionType(String description) {
		this.description = description;
	}
}
