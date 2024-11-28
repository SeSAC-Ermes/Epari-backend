package com.example.epari.exam.exception;

/**
 * 채점 예외
 */
public class GradingException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public GradingException(String message) {
		super(message);
	}

}
