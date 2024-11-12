// FileService.java
package com.example.epari.board.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

	@Value("${file.upload.notice}")
	private String uploadDir;

	public String storeFile(MultipartFile file) {
		String originalFileName = file.getOriginalFilename();
		String storedFileName = createStoredFileName(originalFileName);

		try {
			// 디렉토리가 없으면 생성
			Path uploadPath = Path.of(uploadDir);
			if (!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
			}

			Path targetLocation = uploadPath.resolve(storedFileName);
			Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
			return storedFileName;
		} catch (IOException ex) {
			throw new RuntimeException("Could not store file " + originalFileName, ex);
		}
	}

	public void deleteFile(String storedFileName) {
		try {
			Path filePath = Path.of(uploadDir).resolve(storedFileName);
			Files.deleteIfExists(filePath);
		} catch (IOException ex) {
			throw new RuntimeException("Could not delete file " + storedFileName, ex);
		}
	}

	public String getFileUrl(String storedFileName) {
		return "/files/" + storedFileName;
	}

	private String createStoredFileName(String originalFileName) {
		String ext = originalFileName.substring(originalFileName.lastIndexOf("."));
		return UUID.randomUUID().toString() + ext;
	}

}
