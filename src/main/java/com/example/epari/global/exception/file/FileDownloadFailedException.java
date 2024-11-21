package com.example.epari.global.exception.file;

import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

public class FileDownloadFailedException extends BusinessBaseException {
	public FileDownloadFailedException() {
		super(ErrorCode.FILE_DOWNLOAD_FAILED);
	}

}
