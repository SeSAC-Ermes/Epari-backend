package com.example.epari.global.exception.course;

import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

public class CourseAccessDeniedException extends BusinessBaseException {

	public CourseAccessDeniedException() {
		super(ErrorCode.UNAUTHORIZED_COURSE_ACCESS);
	}

}
