package com.example.epari.global.exception.exam;

/**
 * 채점 예외
 */
public class ExamGradingException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ExamGradingException(String message) {
		super(message);
	}

}
