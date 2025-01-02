package com.example.epari.global.exception.exam;

import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

public class ExamNotFoundException extends BusinessBaseException {
    public ExamNotFoundException() {
        super(ErrorCode.EXAM_NOT_FOUND);
    }
}