package com.example.epari.global.exception;

import lombok.Getter;

/**
 * 입력값 검증 실패 시 발생하는 오류 정보를 담는 클래스
 * 필드명과 해당 필드의 유효성 검증 실패 메시지를 포함
 */
@Getter
public class ValidationError {

	private final String field;

	private final String message;

	private ValidationError(String field, String message) {
		this.field = field;
		this.message = message;
	}

	public static ValidationError of(String field, String message) {
		return new ValidationError(field, message);
	}

}
