package com.example.epari.global.exception;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

@Getter
public class ErrorResponse {

	private final String message;

	private final String code;

	private final List<ValidationError> errors;

	private ErrorResponse(ErrorCode errorCode) {
		this.message = errorCode.getMessage();
		this.code = errorCode.getCode();
		this.errors = new ArrayList<>();
	}

	private ErrorResponse(ErrorCode errorCode, List<ValidationError> errors) {
		this.message = errorCode.getMessage();
		this.code = errorCode.getCode();
		this.errors = errors;
	}

	public static ErrorResponse of(ErrorCode errorCode) {
		return new ErrorResponse(errorCode);
	}

	public static ErrorResponse of(ErrorCode errorCode, List<ValidationError> errors) {
		return new ErrorResponse(errorCode, errors);
	}

}
