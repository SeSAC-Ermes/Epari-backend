package com.example.epari.global.exception.file;

import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

public class FileUploadFailedException extends BusinessBaseException {

	public FileUploadFailedException() {
		super(ErrorCode.FILE_UPLOAD_FAILED);
	}

}
