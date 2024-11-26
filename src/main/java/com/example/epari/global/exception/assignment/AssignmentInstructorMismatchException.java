package com.example.epari.global.exception.assignment;

import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

public class AssignmentInstructorMismatchException extends BusinessBaseException {

	public AssignmentInstructorMismatchException() {
		super(ErrorCode.ASSIGNMENT_INSTRUCTOR_MISMATCH);
	}

}
