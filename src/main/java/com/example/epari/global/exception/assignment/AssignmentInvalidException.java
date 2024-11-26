package com.example.epari.global.exception.assignment;

import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

public class AssignmentInvalidException extends BusinessBaseException {

	public AssignmentInvalidException() {
		super(ErrorCode.ASSIGNMENT_INVALID);
	}

}
