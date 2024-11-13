package com.example.epari.global.exception;

import lombok.Getter;

/**
 * 애플리케이션의 모든 비즈니스 예외의 기본 클래스
 * 구체적인 비즈니스 예외들은 이 클래스를 상속받아 구현
 */
@Getter
public class BusinessBaseException extends RuntimeException {

	private final ErrorCode errorCode;

	public BusinessBaseException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

}
