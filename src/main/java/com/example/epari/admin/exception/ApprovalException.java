package com.example.epari.admin.exception;

import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

/**
 * 승인 간 발생하는 예외를 담는 커스텀 예외 클래스
 */
public class ApprovalException extends BusinessBaseException {

	public ApprovalException(ErrorCode errorCode) {
		super(errorCode);
	}

}
