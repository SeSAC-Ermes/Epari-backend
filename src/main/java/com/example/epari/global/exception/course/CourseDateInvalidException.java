package com.example.epari.global.exception.course;

import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

public class CourseDateInvalidException extends BusinessBaseException {
	public CourseDateInvalidException() {
		super(ErrorCode.COURSE_DATE_INVALID);
	}
}
