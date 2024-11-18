package com.example.epari.board.controller;

import com.example.epari.board.domain.NoticeFile;
import com.example.epari.board.repository.NoticeFileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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

	@GetMapping("/{noticeId}/files/{fileId}/download")
	public ResponseEntity<?> downloadFile(@PathVariable Long noticeId, @PathVariable Long fileId) {
		try {
			NoticeFile noticeFile = noticeFileRepository.findById(fileId)
					.orElseThrow(() -> new EntityNotFoundException("File not found"));

			String fileUrl = noticeFile.getFileUrl();
			String originalFileName = noticeFile.getOriginalFileName();

			GetObjectRequest getObjectRequest = GetObjectRequest.builder()
					.bucket(awsS3Properties.getBucket())
					.key(extractKeyFromUrl(fileUrl))
					.build();

			ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);
			byte[] data = objectBytes.asByteArray();

			String contentType = determineContentType(originalFileName);

			return ResponseEntity.ok()
					.contentType(MediaType.parseMediaType(contentType))
					.header(HttpHeaders.CONTENT_DISPOSITION,
							contentType.startsWith("image/")
									? "inline"
									: "attachment; filename=\"" + URLEncoder.encode(originalFileName, StandardCharsets.UTF_8) + "\"")
					.body(data);
		} catch (Exception e) {
			log.error("File download failed:", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("File download failed: " + e.getMessage());
		}
	}

	private String extractKeyFromUrl(String fileUrl) {
		String prefix = String.format("https://%s.s3.%s.amazonaws.com/",
				awsS3Properties.getBucket(),
				awsS3Properties.getRegion());
		if (!fileUrl.startsWith(prefix)) {
			throw new IllegalArgumentException("Invalid S3 URL format");
		}
		return fileUrl.substring(prefix.length());
	}

	private String determineContentType(String fileName) {
		String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
		switch (extension) {
			case "txt":
				return "text/plain";
			case "pdf":
				return "application/pdf";
			case "doc":
			case "docx":
				return "application/msword";
			case "xls":
			case "xlsx":
				return "application/vnd.ms-excel";
			case "ppt":
			case "pptx":
				return "application/vnd.ms-powerpoint";
			case "jpg":
			case "jpeg":
				return "image/jpeg";
			case "png":
				return "image/png";
			case "gif":
				return "image/gif";
			default:
				return "application/octet-stream";
		}
	}

}


//	@GetMapping("/{fileId}")
//	public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
//		try {
//			NoticeFile noticeFile = noticeFileRepository.findById(fileId)
//					.orElseThrow(() -> new EntityNotFoundException("File not found"));
//
//			Resource resource = fileService.loadFileAsResource(noticeFile.getStoredFileName());
//
//			return ResponseEntity.ok()
//					.contentType(MediaType.APPLICATION_OCTET_STREAM)
//					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + noticeFile.getOriginalFileName() + "\"")
//					.body(resource);
//		} catch (Exception e) {
//			throw new RuntimeException("File download failed", e);
//		}
//	}
