package com.example.epari.global.exception;

import lombok.Getter;

@Getter
public class BusinessBaseException extends RuntimeException {

	private final ErrorCode errorCode;

	public BusinessBaseException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

}
