package com.example.epari.global.exception.file;

import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

public class CourseContentNotFoundException extends BusinessBaseException {
	public CourseContentNotFoundException() {
		super(ErrorCode.COURSE_CONTENT_NOT_FOUND);
	}
}
