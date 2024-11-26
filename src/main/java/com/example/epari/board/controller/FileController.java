package com.example.epari.board.controller;

import com.example.epari.board.domain.NoticeFile;
import com.example.epari.board.repository.NoticeFileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
		try {
			NoticeFile noticeFile = noticeFileRepository.findById(fileId)
					.orElseThrow(() -> new EntityNotFoundException("File not found"));

			GetObjectRequest getObjectRequest = GetObjectRequest.builder()
					.bucket(awsS3Properties.getBucket())
					.key(noticeFile.getStoredFileName())
					.build();

			ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);
			byte[] data = objectBytes.asByteArray();
			ByteArrayResource resource = new ByteArrayResource(data);

			// 파일명 인코딩
			String encodedFileName = URLEncoder.encode(noticeFile.getOriginalFileName(), StandardCharsets.UTF_8)
					.replaceAll("\\+", "%20");

			// 모든 파일을 다운로드 처리하도록 설정
			return ResponseEntity.ok()
					.contentType(MediaType.APPLICATION_OCTET_STREAM)
					.contentLength(data.length)
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
					.body(resource);

		} catch (EntityNotFoundException e) {
			log.error("File not found: {}", fileId);
			throw e;
		} catch (Exception e) {
			log.error("File download failed for fileId: " + fileId, e);
			throw new RuntimeException("File download failed", e);
		}
	}

	/**
	 * S3 URL에서 key 추출
	 */
	private String extractKeyFromUrl(String fileUrl) {
		String prefix = String.format("https://%s.s3.%s.amazonaws.com/",
				awsS3Properties.getBucket(),
				awsS3Properties.getRegion());
		if (!fileUrl.startsWith(prefix)) {
			throw new IllegalArgumentException("Invalid S3 URL format");
		}
		return fileUrl.substring(prefix.length());
	}

	/**
	 * 에러 응답 생성
	 */
	private ResponseEntity<String> createErrorResponse(String message) {
		log.error(message);
		return ResponseEntity.badRequest()
				.body(message);
	}
}

//package com.example.epari.board.controller;
//
//import com.example.epari.board.domain.NoticeFile;
//import com.example.epari.board.repository.NoticeFileRepository;
//import jakarta.persistence.EntityNotFoundException;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.core.io.ByteArrayResource;
//import org.springframework.core.io.Resource;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import software.amazon.awssdk.core.ResponseBytes;
//import software.amazon.awssdk.services.s3.S3Client;
//import software.amazon.awssdk.services.s3.model.GetObjectRequest;
//import software.amazon.awssdk.services.s3.model.GetObjectResponse;
//
//import java.net.URLEncoder;
//import java.nio.charset.StandardCharsets;
//
//@Slf4j
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api/notices")
//public class FileController {
//
//	private final NoticeFileRepository noticeFileRepository;
//
//	private final S3Client s3Client;
//
//	private final com.example.epari.global.config.aws.AwsS3Properties awsS3Properties;
//
//	/**
//	 * 파일 다운로드 처리
//	 */
//	@GetMapping("/{noticeId}/files/{fileId}/download")
//	public ResponseEntity<Resource> downloadFile(@PathVariable Long noticeId, @PathVariable Long fileId) {
//		try {
//			// 파일 정보 조회
//			NoticeFile noticeFile = noticeFileRepository.findById(fileId)
//					.orElseThrow(() -> new EntityNotFoundException("File not found"));
//
//			// S3에서 파일 가져오기
//			GetObjectRequest getObjectRequest = GetObjectRequest.builder()
//					.bucket(awsS3Properties.getBucket())
//					.key(noticeFile.getStoredFileName())  // S3에 저장된 파일명 사용
//					.build();
//
//			ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);
//			byte[] data = objectBytes.asByteArray();
//			ByteArrayResource resource = new ByteArrayResource(data);
//
//			// Content-Type 결정
//			MediaType mediaType = determineMediaType(noticeFile.getOriginalFileName());
//
//			// 파일명 인코딩 (한글 깨짐 방지)
//			String encodedFileName = URLEncoder.encode(noticeFile.getOriginalFileName(), StandardCharsets.UTF_8)
//					.replaceAll("\\+", "%20");
//
//			// 응답 헤더 설정
//			HttpHeaders headers = new HttpHeaders();
//			headers.setContentType(mediaType);
//			headers.setContentLength(data.length);
//			headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName);
//
//			return ResponseEntity.ok()
//					.headers(headers)
//					.body(resource);
//
//		} catch (EntityNotFoundException e) {
//			log.error("File not found: {}", fileId);
//			throw e;
//		} catch (Exception e) {
//			log.error("File download failed for fileId: " + fileId, e);
//			throw new RuntimeException("File download failed", e);
//		}
//	}
//
//	/**
//	 * 파일 확장자에 따른 MediaType 결정
//	 */
//	private MediaType determineMediaType(String fileName) {
//		String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
//		switch (extension) {
//			case "txt":
//				return MediaType.TEXT_PLAIN;
//			case "pdf":
//				return MediaType.APPLICATION_PDF;
//			case "doc":
//			case "docx":
//				return MediaType.parseMediaType("application/msword");
//			case "xls":
//			case "xlsx":
//				return MediaType.parseMediaType("application/vnd.ms-excel");
//			case "ppt":
//			case "pptx":
//				return MediaType.parseMediaType("application/vnd.ms-powerpoint");
//			case "jpg":
//			case "jpeg":
//				return MediaType.IMAGE_JPEG;
//			case "png":
//				return MediaType.IMAGE_PNG;
//			case "gif":
//				return MediaType.IMAGE_GIF;
//			default:
//				return MediaType.APPLICATION_OCTET_STREAM;
//		}
//	}
//
//	/**
//	 * S3 URL에서 key 추출
//	 */
//	private String extractKeyFromUrl(String fileUrl) {
//		String prefix = String.format("https://%s.s3.%s.amazonaws.com/",
//				awsS3Properties.getBucket(),
//				awsS3Properties.getRegion());
//		if (!fileUrl.startsWith(prefix)) {
//			throw new IllegalArgumentException("Invalid S3 URL format");
//		}
//		return fileUrl.substring(prefix.length());
//	}
//
//	/**
//	 * 에러 응답 생성
//	 */
//	private ResponseEntity<String> createErrorResponse(String message) {
//		log.error(message);
//		return ResponseEntity.badRequest()
//				.body(message);
//	}
//
//}
