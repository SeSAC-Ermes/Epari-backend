package com.example.epari.global.exception.assignment;

import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

public class AssignmentNotFoundException extends BusinessBaseException {

	public AssignmentNotFoundException() {
		super(ErrorCode.ASSIGNMENT_NOT_FOUND);
	}

}
