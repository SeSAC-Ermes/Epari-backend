package com.example.epari.global.common.enums;

import lombok.Getter;

@Getter
public enum UserRole {
	INSTRUCTOR("강사"),
	STUDENT("수강생");

	private final String description;

	UserRole(String description) {
		this.description = description;
	}
}
