package com.example.epari.global.exception.auth;

import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

public class SignUpFailedException extends BusinessBaseException {

	public SignUpFailedException() {
		super(ErrorCode.SIGNUP_FAILED);
	}

}
