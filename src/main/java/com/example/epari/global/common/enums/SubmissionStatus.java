package com.example.epari.global.common.enums;

import lombok.Getter;

@Getter
public enum SubmissionStatus {
	NOT_SUBMITTED("미제출"),
	SUBMITTED("제출완료"),
	GRADED("채점완료");

	private final String description;

	SubmissionStatus(String description) {
		this.description = description;
	}
}
