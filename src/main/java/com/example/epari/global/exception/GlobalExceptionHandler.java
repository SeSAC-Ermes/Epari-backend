package com.example.epari.global.exception;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import lombok.extern.slf4j.Slf4j;

/**
 * 애플리케이션 전역의 예외 처리를 담당하는 핸들러
 * 발생하는 모든 예외를 적절한 형식의 응답으로 변환하여 클라이언트에게 반환
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * 비즈니스 예외 처리
	 * BusinessBaseException과 그 하위 예외들을 처리하여 적절한 에러 응답을 생성
	 */
	@ExceptionHandler(BusinessBaseException.class)
	protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessBaseException e) {
		log.error("Business exception occurred: {}", e.getMessage(), e);
		ErrorResponse errorResponse = ErrorResponse.of(e.getErrorCode());
		return ResponseEntity.status(e.getErrorCode().getStatus())
				.body(errorResponse);
	}

	/**
	 * 입력값 검증 실패 예외 처리
	 * @Valid 어노테이션으로 검증 실패 시 발생하는 예외를 처리
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
		List<ValidationError> validationErrors = ex.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(error -> ValidationError.of(
						error.getField(),
						error.getDefaultMessage()))
				.collect(Collectors.toList());

		log.warn("Validation failed: {}", validationErrors);
		ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INVALID_INPUT, validationErrors);
		return ResponseEntity.status(ErrorCode.INVALID_INPUT.getStatus())
				.body(errorResponse);
	}

	/**
	 * 요청한 리소스를 찾을 수 없을 때 발생하는 예외 처리
	 */
	@ExceptionHandler(NoResourceFoundException.class)
	protected ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException e) {
		log.warn("Resource not found: {}", e.getMessage());
		ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.NOT_FOUND);
		return ResponseEntity.status(ErrorCode.NOT_FOUND.getStatus())
				.body(errorResponse);
	}

	/**
	 * 지원하지 않는 HTTP 메서드 호출 시 발생하는 예외 처리
	 */
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	protected ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupported(
			HttpRequestMethodNotSupportedException e) {
		log.warn("Method not supported: {}", e.getMessage());
		ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.BAD_REQUEST);
		return ResponseEntity.status(ErrorCode.BAD_REQUEST.getStatus())
				.body(errorResponse);
	}

	/**
	 * HTTP 요청 메시지를 읽을 수 없을 때 발생하는 예외 처리
	 * 주로 잘못된 JSON 형식이나 타입 불일치로 인해 발생
	 */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	protected ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
			HttpMessageNotReadableException e) {
		log.warn("Message not readable: {}", e.getMessage());
		ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INVALID_INPUT);
		return ResponseEntity.status(ErrorCode.INVALID_INPUT.getStatus())
				.body(errorResponse);
	}

	/**
	 * 필수 요청 파라미터가 누락되었을 때 발생하는 예외 처리
	 */
	@ExceptionHandler(MissingServletRequestParameterException.class)
	protected ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(
			MissingServletRequestParameterException e) {
		log.warn("Missing parameter: {}", e.getMessage());
		ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INVALID_INPUT);
		return ResponseEntity.status(ErrorCode.INVALID_INPUT.getStatus())
				.body(errorResponse);
	}

	/**
	 * 위의 핸들러들에서 처리되지 않은 모든 예외를 처리하는 fallback 핸들러
	 * 예상치 못한 서버 오류를 처리
	 */
	@ExceptionHandler(Exception.class)
	protected ResponseEntity<ErrorResponse> handleException(Exception e) {
		log.error("Unhandled exception occurred", e);
		ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);
		return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
				.body(errorResponse);
	}

}
