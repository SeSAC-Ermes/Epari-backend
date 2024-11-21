package com.example.epari.global.exception.file;

import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

public class FileDeleteFailedException extends BusinessBaseException {
	public FileDeleteFailedException() {
		super(ErrorCode.FILE_DELETE_FAILED);
	}

}
