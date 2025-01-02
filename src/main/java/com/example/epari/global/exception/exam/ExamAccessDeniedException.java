package com.example.epari.global.exception.exam;

import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

public class ExamAccessDeniedException extends BusinessBaseException {
    public ExamAccessDeniedException() {
        super(ErrorCode.UNAUTHORIZED_EXAM_ACCESS);
    }
}