package com.example.epari.global.common.enums;

public enum ExamStatus {
	SUBMITTED("제출완료"),
	GRADING("채점중"),
	GRADED("채점완료");

	private final String description;

	ExamStatus(String description) {
		this.description = description;
	}
}
