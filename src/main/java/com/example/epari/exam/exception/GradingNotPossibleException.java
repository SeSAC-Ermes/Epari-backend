package com.example.epari.exam.exception;

/**
 * 채점 불가 예외
 */
public class GradingNotPossibleException extends GradingException {

	public GradingNotPossibleException(String message) {
		super(message);
	}

}
