package com.example.epari.course.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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

import com.example.epari.course.dto.content.CourseContentRequestDto;
import com.example.epari.course.dto.content.CourseContentResponseDto;
import com.example.epari.course.service.CourseContentService;

import lombok.RequiredArgsConstructor;

/**
 * 강의 컨텐츠 관련 REST API를 처리하는 컨트롤러
 */
@RestController
@RequestMapping("/api/courses/{courseId}/contents")
@RequiredArgsConstructor
@EnableMethodSecurity
public class CourseContentController {

	private final CourseContentService courseContentService;

	/**
	 * 강사가 파일 업로드
	 */
	@PostMapping
	@PreAuthorize("hasRole('INSTRUCTOR')")
	public ResponseEntity<CourseContentResponseDto> uploadContent(
			@PathVariable Long courseId,
			@RequestParam(required = true) String title,
			@RequestParam(required = true) String content,
			@RequestParam(required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
			@RequestParam(required = false) List<MultipartFile> files) {

		CourseContentRequestDto.Upload request = new CourseContentRequestDto.Upload();
		request.setTitle(title);
		request.setContent(content);
		request.setDate(date);
		request.setFiles(files);

		return ResponseEntity.ok(courseContentService.uploadContent(courseId, request));
	}

	/**
	 * 특정 강의 자료를 조회합니다.
	 */
	@GetMapping("/{contentId}")
	public ResponseEntity<CourseContentResponseDto> getContent(
			@PathVariable Long courseId,
			@PathVariable Long contentId) {
		return ResponseEntity.ok(courseContentService.getContent(courseId, contentId));
	}

	/**
	 * 특정 강의의 모든 강의 자료를 조회합니다.
	 */
	@GetMapping
	public ResponseEntity<List<CourseContentResponseDto>> getContents(
			@PathVariable Long courseId) {
		return ResponseEntity.ok(courseContentService.getContents(courseId));
	}

	/**
	 * 기존 강의 자료를 수정합니다.
	 */
	@PutMapping("/{contentId}")
	@PreAuthorize("hasRole('INSTRUCTOR')")
	public ResponseEntity<CourseContentResponseDto> updateContent(
			@PathVariable Long courseId,
			@PathVariable Long contentId,
			@RequestBody CourseContentRequestDto.Update request) {
		return ResponseEntity.ok(courseContentService.updateContent(courseId, contentId, request));
	}

	/**
	 * 특정 강의 자료를 삭제합니다.
	 */
	@DeleteMapping("/{contentId}")
	@PreAuthorize("hasRole('INSTRUCTOR')")
	public ResponseEntity<Void> deleteContent(
			@PathVariable Long courseId,
			@PathVariable Long contentId) {
		courseContentService.deleteContent(courseId, contentId);
		return ResponseEntity.ok().build();
	}

	/**
	 * 여러 강의 자료를 일괄 삭제합니다.
	 */
	@DeleteMapping("/batch")
	@PreAuthorize("hasRole('INSTRUCTOR')")
	public ResponseEntity<Void> deleteContents(
			@PathVariable Long courseId,
			@RequestParam List<Long> contentIds) {
		courseContentService.deleteContents(courseId, contentIds);
		return ResponseEntity.ok().build();
	}

	/**
	 * 강의 자료에 첨부된 파일을 다운로드합니다.
	 */
	@GetMapping("/{contentId}/files/{fileId}/download")
	public ResponseEntity<String> downloadFile(
			@PathVariable Long courseId,
			@PathVariable Long contentId,
			@PathVariable Long fileId) {
		String fileUrl = courseContentService.downloadContent(courseId, contentId, fileId);
		return ResponseEntity.ok(fileUrl);
	}

}
