package com.example.epari.global.exception.assignment;

import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

public class SubmissionInvalidException extends BusinessBaseException {

	public SubmissionInvalidException() {
		super(ErrorCode.SUBMISSION_INVALID);
	}

}
