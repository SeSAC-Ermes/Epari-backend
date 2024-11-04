package com.example.epari.global.common.enums;

import lombok.Getter;

@Getter
public enum DayOfWeek {
	MONDAY("월요일"),
	TUESDAY("화요일"),
	WEDNESDAY("수요일"),
	THURSDAY("목요일"),
	FRIDAY("금요일");

	private final String description;

	DayOfWeek(String description) {
		this.description = description;
	}
}
