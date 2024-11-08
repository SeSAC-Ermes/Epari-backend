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

import lombok.RequiredArgsConstructor;
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
public class S3FileService {

	private final S3Client s3Client;

	private final S3Presigner s3Presigner;

	private final AwsS3Properties awsS3Properties;

	// 허용된 파일 확장자 목록
	private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
			"jpg", "jpeg", "png", "gif", "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt"
	));

	// 최대 파일 크기 (10MB)
	private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

	/**
	 * 파일 업로드
	 */
	public String uploadFile(String directory, MultipartFile file) {
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

			return s3Client.utilities().getUrl(getUrlRequest).toString();
		} catch (IOException e) {
			throw new RuntimeException("파일 업로드 실패: " + e.getMessage(), e);
		}
	}

	/**
	 * 파일 삭제
	 */
	public void deleteFile(String fileUrl) {
		String key = extractKeyFromUrl(fileUrl);

		try {
			DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
					.bucket(awsS3Properties.getBucket())
					.key(key)
					.build();

			s3Client.deleteObject(deleteObjectRequest);
		} catch (NoSuchKeyException e) {
			throw new RuntimeException("파일을 찾을 수 없습니다: " + fileUrl, e);
		} catch (Exception e) {
			throw new RuntimeException("파일 삭제 실패: " + e.getMessage(), e);
		}
	}

	/**
	 * 임시 다운로드 URL 생성 (Presigned URL)
	 */
	public String generatePresignedUrl(String fileUrl, Duration expiration) {
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
			return presignedUrl.toString();
		} catch (Exception e) {
			throw new RuntimeException("Presigned URL 생성 실패: " + e.getMessage(), e);
		}
	}

	/**
	 * 파일 유효성 검사
	 */
	private void validateFile(MultipartFile file) {
		// 파일 존재 여부 확인
		if (file.isEmpty()) {
			throw new IllegalArgumentException("파일이 비어있습니다.");
		}

		// 파일 크기 검사
		if (file.getSize() > MAX_FILE_SIZE) {
			throw new IllegalArgumentException("파일 크기가 허용된 최대 크기를 초과합니다. (최대 10MB)");
		}

		// 파일 확장자 검사
		String extension = extractExt(file.getOriginalFilename()).toLowerCase();
		if (!ALLOWED_EXTENSIONS.contains(extension)) {
			throw new IllegalArgumentException("허용되지 않는 파일 형식입니다: " + extension);
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
			throw new IllegalArgumentException("잘못된 S3 URL 형식입니다.");
		}

		return fileUrl.substring(prefix.length());
	}

}
