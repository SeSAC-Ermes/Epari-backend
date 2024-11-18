package com.example.epari.admin.exception;

import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

/**
 * Cognito 관련 예외
 */
public class CognitoException extends BusinessBaseException {

	public CognitoException(ErrorCode errorCode) {
		super(errorCode);
	}

}
