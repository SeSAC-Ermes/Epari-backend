package com.example.epari.global.exception.course;

import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

/**
 * 강의에 대한 접근 권한이 없는 경우 발생하는 예외
 */
public class CourseAccessDeniedException extends BusinessBaseException {

	public CourseAccessDeniedException() {
		super(ErrorCode.UNAUTHORIZED_COURSE_ACCESS);
	}

}
