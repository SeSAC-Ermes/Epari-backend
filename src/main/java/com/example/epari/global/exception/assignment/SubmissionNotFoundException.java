package com.example.epari.global.exception.assignment;

import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

public class SubmissionNotFoundException extends BusinessBaseException {

	public SubmissionNotFoundException() {
		super(ErrorCode.SUBMISSION_NOT_FOUND);
	}

}
