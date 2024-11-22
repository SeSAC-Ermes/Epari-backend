package com.example.epari.global.exception.file;

import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

public class InvalidFileTypeException extends BusinessBaseException {

	public InvalidFileTypeException() {
		super(ErrorCode.INVALID_FILE_TYPE);
	}

}
