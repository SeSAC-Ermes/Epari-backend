package com.example.epari.global.exception.course;

import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

public class CourseInstructorMismatchException extends BusinessBaseException {
	public CourseInstructorMismatchException() {
		super(ErrorCode.COURSE_INSTRUCTOR_MISMATCH);
	}
}
