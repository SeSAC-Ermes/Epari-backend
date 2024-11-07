package com.example.epari.lecture.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.epari.lecture.dto.content.LectureContentRequestDto;
import com.example.epari.lecture.dto.content.LectureContentResponseDto;
import com.example.epari.lecture.service.LectureContentService;

import lombok.RequiredArgsConstructor;

/**
 * 강의 컨텐츠 관련 REST API를 처리하는 컨트롤러
 */
@RestController
@RequestMapping("/api/lectures/{lectureId}/contents")
@RequiredArgsConstructor
public class LectureContentController {

	private final LectureContentService lectureContentService;

	/**
	 * 강사가 파일 업로드
	 */
	@PostMapping
	@PreAuthorize("hasRole('INSTRUCTOR')")
	public ResponseEntity<LectureContentResponseDto> uploadContent(
			@PathVariable Long lectureId,
			@RequestParam(required = true) String title,
			@RequestParam(required = true) String content,
			@RequestParam(required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
			@RequestParam(required = false) List<MultipartFile> files) {

		LectureContentRequestDto.Upload request = new LectureContentRequestDto.Upload();
		request.setTitle(title);
		request.setContent(content);
		request.setDate(date);
		request.setFiles(files);

		return ResponseEntity.ok(lectureContentService.uploadContent(lectureId, request));
	}

	/**
	 * 특정 강의 자료를 조회합니다.
	 */
	@GetMapping("/{contentId}")
	@PreAuthorize("hasAnyRole('INSTRUCTOR', 'STUDENT')")
	public ResponseEntity<LectureContentResponseDto> getContent(
			@PathVariable Long lectureId,
			@PathVariable Long contentId) {
		return ResponseEntity.ok(lectureContentService.getContent(lectureId, contentId));
	}

	/**
	 * 특정 강의의 모든 강의 자료를 조회합니다.
	 */
	@GetMapping
	@PreAuthorize("hasAnyRole('INSTRUCTOR', 'STDENT')")
	public ResponseEntity<List<LectureContentResponseDto>> getContents(
			@PathVariable Long lectureId) {
		return ResponseEntity.ok(lectureContentService.getContents(lectureId));
	}


	/**
	 * 기존 강의 자료를 수정합니다.
	 */
	@PutMapping("/{contentId}")
	@PreAuthorize("hasRole('INSTRUCTOR')")
	public ResponseEntity<LectureContentResponseDto> updateContent(
			@PathVariable Long lectureId,
			@PathVariable Long contentId,
			@RequestBody LectureContentRequestDto.Update request) {
		return ResponseEntity.ok(lectureContentService.updateContent(lectureId, contentId, request));
	}


	/**
	 * 특정 강의 자료를 삭제합니다.
	 */
	@DeleteMapping("/{contentId}")
	@PreAuthorize("hasRole('INSTRUCTOR')")
	public ResponseEntity<Void> deleteContent(
			@PathVariable Long lectureId,
			@PathVariable Long contentId) {
		lectureContentService.deleteContent(lectureId, contentId);
		return ResponseEntity.ok().build();
	}

	/**
	 * 여러 강의 자료를 일괄 삭제합니다.
	 */
	@DeleteMapping("/batch")
	@PreAuthorize("hasRole('INSTRUCTOR')")
	public ResponseEntity<Void> deleteContents(
			@PathVariable Long lectureId,
			@RequestParam List<Long> contentIds) {
		lectureContentService.deleteContents(lectureId, contentIds);
		return ResponseEntity.ok().build();
	}

	/**
	 * 강의 자료에 첨부된 파일을 다운로드합니다.
	 */
	@GetMapping("/{contentId}/files/{fileId}/download")
	@PreAuthorize("hasAnyRole('INSTRUCTOR', 'STUDENT')")
	public ResponseEntity<String> downloadFile(
			@PathVariable Long lectureId,
			@PathVariable Long contentId,
			@PathVariable Long fileId) {
		String fileUrl = lectureContentService.downloadContent(lectureId, contentId, fileId);
		return ResponseEntity.ok(fileUrl);
	}

}
