package com.example.epari.global.exception;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;

import lombok.Getter;

/**
 * API 에러 응답의 표준 형식을 정의하는 클래스
 * 에러 메시지, 코드 및 상세 검증 오류 목록을 포함
 */
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

	    public static ResponseEntity<ErrorResponse> toResponseEntity(ErrorCode errorCode) {
        return ResponseEntity
            .status(errorCode.getStatus())
            .body(ErrorResponse.of(errorCode));
    }

    public static ResponseEntity<ErrorResponse> toResponseEntity(ErrorCode errorCode, List<ValidationError> errors) {
        return ResponseEntity
            .status(errorCode.getStatus())
            .body(ErrorResponse.of(errorCode, errors));

}
}
