package com.example.epari.global.exception.file;

import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

public class FileSizeExceededException extends BusinessBaseException {

	public FileSizeExceededException() {
		super(ErrorCode.FILE_SIZE_EXCEEDED);
	}

}
