package com.example.epari.course.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.epari.course.dto.content.CourseContentCursorDto;
import com.example.epari.course.dto.content.CourseContentListResponseDto;
import com.example.epari.course.dto.content.CourseContentRequestDto;
import com.example.epari.course.dto.content.CourseContentResponseDto;
import com.example.epari.course.dto.content.CourseContentSearchRequestDto;
import com.example.epari.course.dto.content.PageResponse;
import com.example.epari.course.service.CourseContentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 강의 컨텐츠 관련 REST API를 처리하는 컨트롤러
 */
@RestController
@RequestMapping("/api/courses/{courseId}/contents")
@RequiredArgsConstructor
@EnableMethodSecurity
@Slf4j
public class CourseContentController {

	private final CourseContentService courseContentService;

	/**
	 * 강사가 파일 업로드
	 */
	@PostMapping
	@PreAuthorize("hasRole('INSTRUCTOR')")
	public ResponseEntity<CourseContentResponseDto> uploadContent(
			@PathVariable Long courseId,
			@Valid @ModelAttribute CourseContentRequestDto.Upload request) {
		log.info("Content upload request received for course: {}", courseId);
		request.setDate(LocalDate.now());
		return ResponseEntity.ok(courseContentService.uploadContent(courseId, request));
	}

	/**
	 * 특정 강의 자료를 조회합니다.
	 */
	@GetMapping("/{contentId}")
	public ResponseEntity<CourseContentResponseDto> getContent(
			@PathVariable Long courseId,
			@PathVariable Long contentId) {
		log.info("Fetching content: {} for course: {}", contentId, courseId);
		return ResponseEntity.ok(courseContentService.getContent(courseId, contentId));
	}

	/**
	 * 특정 강의의 모든 강의 자료를 조회합니다.
	 */
	@GetMapping
	public ResponseEntity<CourseContentListResponseDto> getContents(
			@PathVariable Long courseId,
			@RequestParam(required = false) Long cursorId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate cursorDate) {

		CourseContentCursorDto cursor = null;
		if (cursorId != null && cursorDate != null) {
			cursor = CourseContentCursorDto.of(cursorId, cursorDate);
		}

		log.info("Fetching course contents - courseId: {}, cursor: {}", courseId, cursor);

		return ResponseEntity.ok(courseContentService.getContents(courseId, cursor));
	}

	/**
	 * 특정 강의에 강의 자료 검색 제목, 내용
	 */
	@GetMapping("/search")
	public ResponseEntity<CourseContentListResponseDto> searchContents(
			@PathVariable Long courseId,
			@ModelAttribute CourseContentSearchRequestDto searchRequest,
			@RequestParam(required = false) Long cursorId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate cursorDate) {

		CourseContentCursorDto cursor = null;
		if (cursorId != null && cursorDate != null) {
			cursor = CourseContentCursorDto.of(cursorId, cursorDate);
		}

		log.info("Searching course contents - courseId: {}, searchRequest: {}, cursor: {}",
				courseId, searchRequest, cursor);

		return ResponseEntity.ok(courseContentService.searchContents(courseId, searchRequest, cursor));
	}

	/**
	 * 기존 강의 자료를 수정합니다.
	 */
	@PutMapping("/{contentId}")
	@PreAuthorize("hasRole('INSTRUCTOR')")
	public ResponseEntity<CourseContentResponseDto> updateContent(
			@PathVariable Long courseId,
			@PathVariable Long contentId,
			@Valid @ModelAttribute CourseContentRequestDto.Update request) {
		log.info("Content update request received - courseId: {}, contentId: {}", courseId, contentId);
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
		log.info("Content deletion request received - courseId: {}, contentId: {}", courseId, contentId);
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
		log.info("Batch content deletion request received - courseId: {}, contentIds: {}",
				courseId, contentIds);
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
		log.info("File download request received - courseId: {}, contentId: {}, fileId: {}",
				courseId, contentId, fileId);
		String fileUrl = courseContentService.downloadContent(courseId, contentId, fileId);
		return ResponseEntity.ok(fileUrl);
	}

	/**
	 * 강의 자료의 특정 파일을 삭제합니다.
	 */
	@DeleteMapping("/{contentId}/files/{fileId}")
	@PreAuthorize("hasRole('INSTRUCTOR')")
	public ResponseEntity<CourseContentResponseDto> deleteFile(
			@PathVariable Long courseId,
			@PathVariable Long contentId,
			@PathVariable Long fileId) {
		log.info("File deletion request received - courseId: {}, contentId: {}, fileId: {}",
				courseId, contentId, fileId);
		return ResponseEntity.ok(courseContentService.deleteFile(courseId, contentId, fileId));
	}

	/**
	 * 당일 날짜의 강의 자료를 조회합니다.
	 */
	@GetMapping("/today")
	public ResponseEntity<List<CourseContentResponseDto>> getTodayContents(
			@PathVariable Long courseId) {
		log.info("Fetching today's contents for course: {}", courseId);
		return ResponseEntity.ok(courseContentService.getTodayContents(courseId));
	}

	/**
	 * 오프셋 기반 페이지네이션
	 */
	@GetMapping("/offset")
	public ResponseEntity<PageResponse<CourseContentResponseDto>> getContentsWithOffset(
			@PathVariable Long courseId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "date") String sortBy,
			@RequestParam(defaultValue = "desc") String direction) {

		log.info("Fetching contents with offset - courseId: {}, page: {}, size: {}", courseId, page, size);
		return ResponseEntity.ok(courseContentService.getContentsWithOffset(courseId, page, size, sortBy, direction));
	}

}
