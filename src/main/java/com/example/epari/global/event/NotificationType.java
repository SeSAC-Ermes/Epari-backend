package com.example.epari.global.event;

import lombok.Getter;

/**
 * 알림 타입 Enum 클래스
 */
@Getter
public enum NotificationType {

	STUDENT_APPROVED("수강생 승인", "student-approved.html"),
	INSTRUCTOR_APPROVED("강사 승인", "instructor-approved.html"),
	USER_REJECTED("사용자 반려", "user-rejected.html"),
	// 과제 채점 알림
	GRADE("과제 평가가 완료되었습니다.", "assignment-grade.html"),;


	private final String description;

	private final String templatePath;

	NotificationType(String description, String templatePath) {
		this.description = description;
		this.templatePath = templatePath;
	}

}
