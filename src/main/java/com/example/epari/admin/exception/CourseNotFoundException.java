package com.example.epari.admin.exception;

import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

/**
 * 강의를 찾을 수 없을 때 발생하는 예외
 */
public class CourseNotFoundException extends BusinessBaseException {

	public CourseNotFoundException() {
		super(ErrorCode.COURSE_NOT_FOUND);
	}

}
