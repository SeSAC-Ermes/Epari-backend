package com.example.epari.global.exception.auth;

import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

public class VerificationCodeExpiredException extends BusinessBaseException {

	public VerificationCodeExpiredException() {
		super(ErrorCode.VERIFICATION_CODE_EXPIRED);
	}

}
