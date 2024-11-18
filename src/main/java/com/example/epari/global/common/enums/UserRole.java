package com.example.epari.global.common.enums;

import lombok.Getter;

/**
 * 사용자 역할을 정의하는 Enum 클래스
 */
@Getter
public enum UserRole {
	INSTRUCTOR("강사"),
	STUDENT("수강생"),
	ADMIN("관리자");

	private final String description;

	UserRole(String description) {
		this.description = description;
	}
}
