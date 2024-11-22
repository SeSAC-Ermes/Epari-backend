package com.example.epari.global.exception.attendance;

import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

public class AttendanceFutureCourseException extends BusinessBaseException {

	public AttendanceFutureCourseException() {
		super(ErrorCode.ATTENDANCE_FUTURE_COURSE);
	}

}
