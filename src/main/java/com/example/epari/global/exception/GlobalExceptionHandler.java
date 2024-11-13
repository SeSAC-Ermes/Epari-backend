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

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessBaseException.class)
	protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessBaseException e) {
		log.error("Business exception occurred: {}", e.getMessage(), e);
		ErrorResponse errorResponse = ErrorResponse.of(e.getErrorCode());
		return ResponseEntity.status(e.getErrorCode().getStatus())
				.body(errorResponse);
	}

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

	@ExceptionHandler(NoResourceFoundException.class)
	protected ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException e) {
		log.warn("Resource not found: {}", e.getMessage());
		ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.NOT_FOUND);
		return ResponseEntity.status(ErrorCode.NOT_FOUND.getStatus())
				.body(errorResponse);
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	protected ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupported(
			HttpRequestMethodNotSupportedException e) {
		log.warn("Method not supported: {}", e.getMessage());
		ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.BAD_REQUEST);
		return ResponseEntity.status(ErrorCode.BAD_REQUEST.getStatus())
				.body(errorResponse);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	protected ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
			HttpMessageNotReadableException e) {
		log.warn("Message not readable: {}", e.getMessage());
		ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INVALID_INPUT);
		return ResponseEntity.status(ErrorCode.INVALID_INPUT.getStatus())
				.body(errorResponse);
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	protected ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(
			MissingServletRequestParameterException e) {
		log.warn("Missing parameter: {}", e.getMessage());
		ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INVALID_INPUT);
		return ResponseEntity.status(ErrorCode.INVALID_INPUT.getStatus())
				.body(errorResponse);
	}

	@ExceptionHandler(Exception.class)
	protected ResponseEntity<ErrorResponse> handleException(Exception e) {
		log.error("Unhandled exception occurred", e);
		ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);
		return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
				.body(errorResponse);
	}

}
