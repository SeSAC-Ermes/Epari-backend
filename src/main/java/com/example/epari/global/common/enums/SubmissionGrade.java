package com.example.epari.global.common.enums;

import lombok.Getter;

@Getter
public enum SubmissionGrade {
	PASS("pass"),
	NONE_PASS("none pass"),
	UNDER_REVIEW("검토중");

	private final String description;

	SubmissionGrade(String description) {
		this.description = description;
	}
}
