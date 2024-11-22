package com.example.epari.global.exception.auth;

import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

public class InstructorNotFoundException extends BusinessBaseException {

	public InstructorNotFoundException() {
		super(ErrorCode.INSTRUCTOR_NOT_FOUND);
	}

}
