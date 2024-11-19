package com.example.epari.global.exception.attendance;

import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

public class AttendanceNotFoundException extends BusinessBaseException {
	public AttendanceNotFoundException() {
		super(ErrorCode.ATTENDANCE_NOT_FOUND);
	}

}
