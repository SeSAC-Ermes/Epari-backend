package com.example.epari.global.exception;

import lombok.Getter;

@Getter
public class ValidationError {

	private final String field;

	private final String message;

	private ValidationError(String field, String message) {
		this.field = field;
		this.message = message;
	}

	public static ValidationError of(String field, String message) {
		return new ValidationError(field, message);
	}

}
