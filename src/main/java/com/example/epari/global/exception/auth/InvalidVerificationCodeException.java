package com.example.epari.global.exception.auth;

import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

public class InvalidVerificationCodeException extends BusinessBaseException {

	public InvalidVerificationCodeException() {
		super(ErrorCode.INVALID_VERIFICATION_CODE);
	}

}
