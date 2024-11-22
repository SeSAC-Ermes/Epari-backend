package com.example.epari.global.common.enums;

import lombok.Getter;

@Getter
public enum SubmissionGrade {
	PASS("통과"),
	NONE_PASS("미통과"),
	UNDER_REVIEW("검토중");

	private final String description;

	SubmissionGrade(String description) {
		this.description = description;
	}
}
