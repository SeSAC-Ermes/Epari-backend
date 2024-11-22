package com.example.epari.global.exception.file;

import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

public class InvalidFileUrlException extends BusinessBaseException {

	public InvalidFileUrlException() {
		super(ErrorCode.INVALID_FILE_URL);
	}

}
