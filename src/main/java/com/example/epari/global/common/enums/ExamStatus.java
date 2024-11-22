package com.example.epari.global.common.enums;

public enum ExamStatus {
	SCHEDULED("예정"),
	IN_PROGRESS("진행중"),
	NOT_SUBMITTED("미제출"),
	SUBMITTED("제출완료"),
	GRADING("채점중"),
	GRADED("채점완료"),
	COMPLETED("종료");

	private final String description;

	ExamStatus(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}
