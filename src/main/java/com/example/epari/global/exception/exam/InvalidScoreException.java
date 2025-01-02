package com.example.epari.global.exception.exam;

/**
 * 점수 유효성 예외
 */
public class InvalidScoreException extends ExamGradingException {

	public InvalidScoreException(String message) {
		super(message);
	}

}
