package com.example.epari.global.exception;

import lombok.Getter;

@Getter
public class ErrorResponse {

	private final String message;

	private final String code;

	private ErrorResponse(ErrorCode errorCode) {
		this.message = errorCode.getMessage();
		this.code = errorCode.getCode();
	}

	public static ErrorResponse of(ErrorCode errorCode) {
		return new ErrorResponse(errorCode);
	}

}
