package com.example.epari.global.exception.course;

import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

public class CourseNotFoundException extends BusinessBaseException {

	public CourseNotFoundException() {
		super(ErrorCode.COURSE_NOT_FOUND);
	}

}
