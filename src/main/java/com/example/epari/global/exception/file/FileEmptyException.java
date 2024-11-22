package com.example.epari.global.exception.file;

import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

public class FileEmptyException extends BusinessBaseException {

	public FileEmptyException() {
		super(ErrorCode.FILE_EMPTY);
	}

}
