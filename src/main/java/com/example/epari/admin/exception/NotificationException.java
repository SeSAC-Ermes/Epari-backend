package com.example.epari.admin.exception;

import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

/**
 * 알림 관련 커스텀 예외
 */
public class NotificationException extends BusinessBaseException {

	public NotificationException(ErrorCode errorCode) {
		super(errorCode);
	}

}
