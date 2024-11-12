package com.example.epari.board.service;

import com.example.epari.board.dto.FileInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
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
		String storedFileName = generateStoredFileName(originalFileName);

		try {
			File directory = new File(uploadDir);
			if (!directory.exists()) {
				directory.mkdirs();
			}

			Path targetLocation = Path.of(uploadDir, storedFileName);
			Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
			return storedFileName;
		} catch (IOException ex) {
			throw new RuntimeException("Could not store file " + originalFileName, ex);
		}
	}

	public String getFileUrl(String storedFileName) {
		return "/api/files/notices/" + storedFileName;
	}

	public Resource loadFileAsResource(String fileName) {
		try {
			Path filePath = Path.of(uploadDir, fileName);
			Resource resource = new UrlResource(filePath.toUri());
			if (resource.exists()) {
				return resource;
			} else {
				throw new RuntimeException("File not found: " + fileName);
			}
		} catch (MalformedURLException ex) {
			throw new RuntimeException("File not found: " + fileName);
		}
	}

	public FileInfoDto uploadFile(MultipartFile file) {
		if (file.isEmpty()) {
			throw new IllegalArgumentException("File is empty");
		}

		String originalFileName = file.getOriginalFilename();
		String storedFileName = generateStoredFileName(originalFileName);
		String fileUrl = generateFileUrl(storedFileName);

		try {
			File directory = new File(uploadDir);
			if (!directory.exists()) {
				directory.mkdirs();
			}

			Path targetLocation = Path.of(uploadDir, storedFileName);
			Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

			return FileInfoDto.builder()
					.originalFileName(originalFileName)
					.storedFileName(storedFileName)
					.fileSize(file.getSize())
					.fileUrl(fileUrl)
					.build();
		} catch (IOException ex) {
			throw new RuntimeException("Could not store file " + originalFileName, ex);
		}
	}

	public void deleteFile(String storedFileName) {
		try {
			Path filePath = Path.of(uploadDir, storedFileName);
			Files.deleteIfExists(filePath);
		} catch (IOException ex) {
			throw new RuntimeException("Could not delete file " + storedFileName, ex);
		}
	}

	private String generateStoredFileName(String originalFileName) {
		return UUID.randomUUID().toString() + getFileExtension(originalFileName);
	}

	private String getFileExtension(String fileName) {
		return fileName.substring(fileName.lastIndexOf("."));
	}

	private String generateFileUrl(String storedFileName) {
		return "/api/files/notices/" + storedFileName;
	}

}
