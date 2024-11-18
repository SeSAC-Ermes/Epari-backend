package com.example.epari.global.exception.auth;

import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

public class AuthUserNotFoundException extends BusinessBaseException {

	public AuthUserNotFoundException() {
		super(ErrorCode.USER_NOT_FOUND);
	}

}
