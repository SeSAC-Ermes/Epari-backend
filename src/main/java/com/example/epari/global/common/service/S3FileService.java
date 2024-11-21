package com.example.epari.global.common.service;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.epari.global.config.aws.AwsS3Properties;
import com.example.epari.global.exception.file.CourseFileNotFoundException;
import com.example.epari.global.exception.file.FileDeleteFailedException;
import com.example.epari.global.exception.file.FileDownloadFailedException;
import com.example.epari.global.exception.file.FileEmptyException;
import com.example.epari.global.exception.file.FileSizeExceededException;
import com.example.epari.global.exception.file.FileUploadFailedException;
import com.example.epari.global.exception.file.InvalidFileTypeException;
import com.example.epari.global.exception.file.InvalidFileUrlException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3FileService {

	private final S3Client s3Client;

	private final S3Presigner s3Presigner;

	private final AwsS3Properties awsS3Properties;

	// 허용된 파일 확장자 목록
	private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
			"jpg", "jpeg", "png", "gif", "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt"
	));

	// 최대 파일 크기 (10MB)
	private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;

	/**
	 * 파일 업로드
	 */
	public String uploadFile(String directory, MultipartFile file) {
		log.info("Starting file upload to directory: {}", directory);
		validateFile(file);

		String originalFilename = file.getOriginalFilename();
		String storedFileName = createStoredFileName(originalFilename);
		String fullPath = directory + "/" + storedFileName;

		try {
			PutObjectRequest putObjectRequest = PutObjectRequest.builder()
					.bucket(awsS3Properties.getBucket())
					.key(fullPath)
					.contentType(file.getContentType())
					.contentLength(file.getSize())
					.build();

			s3Client.putObject(putObjectRequest,
					RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

			GetUrlRequest getUrlRequest = GetUrlRequest.builder()
					.bucket(awsS3Properties.getBucket())
					.key(fullPath)
					.build();

			String fileUrl = s3Client.utilities().getUrl(getUrlRequest).toString();
			log.info("File successfully uploaded: {}", fileUrl);
			return fileUrl;
		} catch (IOException e) {
			log.error("Failed to upload file: {}", originalFilename, e);
			throw new FileUploadFailedException();
		}
	}

	/**
	 * 파일 삭제
	 */
	public void deleteFile(String fileUrl) {
		log.info("Attempting to delete file: {}", fileUrl);
		String key = extractKeyFromUrl(fileUrl);

		try {
			DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
					.bucket(awsS3Properties.getBucket())
					.key(key)
					.build();

			s3Client.deleteObject(deleteObjectRequest);
			log.info("File successfully deleted: {}", fileUrl);
		} catch (NoSuchKeyException e) {
			log.error("File not found: {}", fileUrl);
			throw new CourseFileNotFoundException();
		} catch (Exception e) {
			log.error("Failed to delete file: {}", fileUrl, e);
			throw new FileDeleteFailedException();
		}
	}

	/**
	 * 임시 다운로드 URL 생성 (Presigned URL)
	 */
	public String generatePresignedUrl(String fileUrl, Duration expiration) {
		log.info("Generating presigned URL for: {}", fileUrl);
		String key = extractKeyFromUrl(fileUrl);

		try {
			GetObjectRequest getObjectRequest = GetObjectRequest.builder()
					.bucket(awsS3Properties.getBucket())
					.key(key)
					.build();

			GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
					.signatureDuration(expiration)
					.getObjectRequest(getObjectRequest)
					.build();

			URL presignedUrl = s3Presigner.presignGetObject(presignRequest).url();
			log.info("Successfully generated presigned URL for: {}", fileUrl);
			return presignedUrl.toString();
		} catch (Exception e) {
			log.error("Failed to generate presigned URL for: {}", fileUrl, e);
			throw new FileDownloadFailedException();
		}
	}

	/**
	 * 파일 유효성 검사
	 */
	private void validateFile(MultipartFile file) {
		// 파일 존재 여부 확인
		if (file.isEmpty()) {
			log.warn("Empty file detected");
			throw new FileEmptyException();
		}

		// 파일 크기 검사
		if (file.getSize() > MAX_FILE_SIZE) {
			log.warn("File size exceeded: {} bytes", file.getSize());
			throw new FileSizeExceededException();
		}

		// 파일 확장자 검사
		String extension = extractExt(file.getOriginalFilename()).toLowerCase();
		if (!ALLOWED_EXTENSIONS.contains(extension)) {
			log.warn("Invalid file type detected: {}", extension);
			throw new InvalidFileTypeException();
		}
	}

	private String createStoredFileName(String originalFilename) {
		String uuid = UUID.randomUUID().toString();
		String ext = extractExt(originalFilename);
		return uuid + "." + ext;
	}

	private String extractExt(String originalFilename) {
		int pos = originalFilename.lastIndexOf(".");
		return originalFilename.substring(pos + 1);
	}

	private String extractKeyFromUrl(String fileUrl) {
		String bucket = awsS3Properties.getBucket();
		String region = awsS3Properties.getRegion();
		String prefix = String.format("https://%s.s3.%s.amazonaws.com/", bucket, region);

		if (!fileUrl.startsWith(prefix)) {
			log.error("Invalid S3 URL format: {}", fileUrl);
			throw new InvalidFileUrlException();
		}

		return fileUrl.substring(prefix.length());
	}

}
