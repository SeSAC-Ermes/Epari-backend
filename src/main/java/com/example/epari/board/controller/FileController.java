package com.example.epari.board.controller;

import com.example.epari.board.domain.NoticeFile;
import com.example.epari.board.repository.NoticeFileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notices")
public class FileController {

	private final NoticeFileRepository noticeFileRepository;

	private final S3Client s3Client;

	private final com.example.epari.global.config.aws.AwsS3Properties awsS3Properties;

	/**
	 * 파일 다운로드 처리
	 */
	@GetMapping("/{noticeId}/files/{fileId}/download")
	public ResponseEntity<Resource> downloadFile(@PathVariable Long noticeId, @PathVariable Long fileId) {
		log.info("Downloading file. NoticeId: {}, FileId: {}", noticeId, fileId);

		try {
			// 1. 파일 정보 조회
			NoticeFile noticeFile = noticeFileRepository.findById(fileId)
					.orElseThrow(() -> new EntityNotFoundException("File not found with ID: " + fileId));
			log.info("Found file: {}, stored filename: {}", noticeFile.getOriginalFileName(), noticeFile.getStoredFileName());

			// 2. S3 키 생성 - fileUrl에서 추출
			String s3Key = extractKeyFromUrl(noticeFile.getFileUrl());
			log.info("Extracted S3 key from URL: {}", s3Key);

			// 3. S3에서 파일 가져오기
			GetObjectRequest getObjectRequest = GetObjectRequest.builder()
					.bucket(awsS3Properties.getBucket())
					.key(s3Key)  // storedFileName 대신 추출된 키 사용
					.build();
			log.info("Requesting from S3 bucket: {}, key: {}", awsS3Properties.getBucket(), s3Key);

			try {
				ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);
				byte[] data = objectBytes.asByteArray();
				log.info("Successfully retrieved file from S3. Size: {} bytes", data.length);

				ByteArrayResource resource = new ByteArrayResource(data);

				// 파일명 인코딩
				String encodedFileName = URLEncoder.encode(noticeFile.getOriginalFileName(), StandardCharsets.UTF_8)
						.replaceAll("\\+", "%20");

				return ResponseEntity.ok()
						.contentType(MediaType.APPLICATION_OCTET_STREAM)
						.contentLength(data.length)
						.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
						.header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
						.body(resource);

			} catch (Exception s3Error) {
				log.error("Error retrieving file from S3. Bucket: {}, Key: {}, Error: {}",
						awsS3Properties.getBucket(), s3Key, s3Error.getMessage());
				throw new RuntimeException("Failed to retrieve file from S3", s3Error);
			}

		} catch (EntityNotFoundException e) {
			log.error("File not found: {}", e.getMessage());
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found", e);
		} catch (Exception e) {
			log.error("Error downloading file: {}", e.getMessage());
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to download file", e);
		}
	}


	/**
	 * S3 URL에서 key 추출
	 */
	private String extractKeyFromUrl(String fileUrl) {
		if (fileUrl == null || fileUrl.isEmpty()) {
			throw new IllegalArgumentException("FileUrl is null or empty");
		}

		String prefix = String.format("https://%s.s3.%s.amazonaws.com/",
				awsS3Properties.getBucket(),
				awsS3Properties.getRegion());

		if (!fileUrl.startsWith(prefix)) {
			log.warn("FileUrl does not match expected format: {}", fileUrl);
			// URL에서 마지막 / 이후의 부분을 키로 사용
			int lastSlashIndex = fileUrl.lastIndexOf('/');
			if (lastSlashIndex >= 0 && lastSlashIndex < fileUrl.length() - 1) {
				return fileUrl.substring(lastSlashIndex + 1);
			}
			throw new IllegalArgumentException("Invalid S3 URL format: " + fileUrl);
		}

		String key = fileUrl.substring(prefix.length());
		log.info("Extracted S3 key: {}", key);
		return key;
	}


	/**
	 * 에러 응답 생성
	 */
	private ResponseEntity<String> createErrorResponse(String message) {
		log.error("Creating error response: {}", message);
		return ResponseEntity.badRequest()
				.body(message);
	}

}
