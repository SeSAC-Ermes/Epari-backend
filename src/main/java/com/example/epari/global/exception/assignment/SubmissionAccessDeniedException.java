package com.example.epari.global.exception.assignment;

import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

public class SubmissionAccessDeniedException extends BusinessBaseException {

	public SubmissionAccessDeniedException() {
		super(ErrorCode.UNAUTHORIZED_SUBMISSION_ACCESS);
	}

}
