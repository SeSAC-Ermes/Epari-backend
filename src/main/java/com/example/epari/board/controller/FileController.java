package com.example.epari.board.controller;

import com.example.epari.board.domain.NoticeFile;
import com.example.epari.board.repository.NoticeFileRepository;
import com.example.epari.board.service.FileService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

@RestController
@RequestMapping("/api/files/notices")
@RequiredArgsConstructor
public class FileController {

	private final FileService fileService;

	private final NoticeFileRepository noticeFileRepository;

	@GetMapping("/{fileName}")
	public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
		Resource resource = fileService.loadFileAsResource(fileName);

		String contentType = null;
		try {
			contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
		} catch (IOException ex) {
			contentType = "application/octet-stream";
		}

		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(contentType))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
				.body(resource);
	}


	// 첨부파일 다운로드
	@GetMapping("/{fileId}")
	public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
		try {
			NoticeFile noticeFile = noticeFileRepository.findById(fileId)
					.orElseThrow(() -> new EntityNotFoundException("File not found"));

			Resource resource = fileService.loadFileAsResource(noticeFile.getStoredFileName());

			return ResponseEntity.ok()
					.contentType(MediaType.APPLICATION_OCTET_STREAM)
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + noticeFile.getOriginalFileName() + "\"")
					.body(resource);
		} catch (Exception e) {
			throw new RuntimeException("File download failed", e);
		}
	}

}
