package com.example.epari.global.exception.exam;

import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

public class ExamResultNotFoundException extends BusinessBaseException {
	public ExamResultNotFoundException() {
		super(ErrorCode.EXAM_RESULT_NOT_FOUND);
	}

}
