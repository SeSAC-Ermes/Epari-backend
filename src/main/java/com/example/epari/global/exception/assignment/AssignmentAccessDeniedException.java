package com.example.epari.global.exception.assignment;

import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

public class AssignmentAccessDeniedException extends BusinessBaseException {

	public AssignmentAccessDeniedException() {
		super(ErrorCode.UNAUTHORIZED_ASSIGNMENT_ACCESS);
	}

}
