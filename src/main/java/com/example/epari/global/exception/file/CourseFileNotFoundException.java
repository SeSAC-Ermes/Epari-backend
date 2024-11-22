package com.example.epari.global.exception.file;

import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

public class CourseFileNotFoundException extends BusinessBaseException {
	public CourseFileNotFoundException() {
		super(ErrorCode.FILE_NOT_FOUND);
	}
}
