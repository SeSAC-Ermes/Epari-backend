package com.example.epari.exam.exception;

/**
 * 점수 유효성 예외
 */
public class InvalidScoreException extends GradingException {

	public InvalidScoreException(String message) {
		super(message);
	}

}
