package com.example.epari.global.common.enums;

import lombok.Getter;

@Getter
public enum AttendanceStatus {
	PRESENT("출석"),
	LATE("지각"),
	ABSENT("결석"),
	SICK_LEAVE("병결");

	private final String description;

	AttendanceStatus(String description) {
		this.description = description;
	}
}
