package com.example.epari.global.exception.exam;

import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

public class ExamPeriodException extends BusinessBaseException {
    public ExamPeriodException(ErrorCode errorCode) {
        super(errorCode);  // EXAM_NOT_STARTED, EXAM_ALREADY_ENDED, EXAM_TIME_EXPIRED 중 하나
    }
}