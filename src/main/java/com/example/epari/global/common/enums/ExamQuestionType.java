package com.example.epari.global.common.enums;

import lombok.Getter;

public enum ExamQuestionType {
	MULTIPLE_CHOICE("객관식"),
	SUBJECTIVE("주관식");

	private final String description;

	ExamQuestionType(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}
