package com.example.epari.global.exception.auth;

import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

public class AuthenticationException extends BusinessBaseException {

	public AuthenticationException(ErrorCode errorCode) {
		super(errorCode);
	}

}
