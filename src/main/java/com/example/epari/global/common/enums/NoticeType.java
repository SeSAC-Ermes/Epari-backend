package com.example.epari.global.common.enums;

import lombok.Getter;

@Getter
public enum NoticeType {
	GLOBAL("전체 공지사항"),
	LECTURE("강의 공지사항");

	private final String description;

	NoticeType(String description) {
		this.description = description;
	}
}
