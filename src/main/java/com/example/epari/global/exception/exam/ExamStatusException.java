package com.example.epari.global.exception.exam;

import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

public class ExamStatusException extends BusinessBaseException {
    public ExamStatusException(ErrorCode errorCode) {
        super(errorCode);  // EXAM_RESULT_INVALID_STATUS, EXAM_ALREADY_SUBMITTED 등
    }
}