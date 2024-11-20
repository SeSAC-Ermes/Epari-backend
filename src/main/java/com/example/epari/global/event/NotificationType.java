package com.example.epari.global.event;

import lombok.Getter;

/**
 * 알림 타입 Enum 클래스
 */
@Getter
public enum NotificationType {

	USER_APPROVED("사용자 승인", "user-approved.html"),
	USER_REJECTED("사용자 반려", "user-rejected.html");

	private final String description;

	private final String templatePath;

	NotificationType(String description, String templatePath) {
		this.description = description;
		this.templatePath = templatePath;
	}

}
