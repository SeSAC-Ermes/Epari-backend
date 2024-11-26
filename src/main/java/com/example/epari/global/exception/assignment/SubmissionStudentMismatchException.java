package com.example.epari.global.exception.assignment;

import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

public class SubmissionStudentMismatchException extends BusinessBaseException {

	public SubmissionStudentMismatchException() {
		super(ErrorCode.SUBMISSION_STUDENT_MISMATCH);
	}

}
