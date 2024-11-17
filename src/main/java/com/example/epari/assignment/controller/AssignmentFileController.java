package com.example.epari.assignment.controller;

import com.example.epari.assignment.dto.assignment.AssignmentResponseDto;
import com.example.epari.assignment.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/courses/{courseId}/assignments/{assignmentId}/files")
public class AssignmentFileController {

	private final AssignmentService assignmentService;

	/**
	 * 파일 다운로드 URL 조회
	 */
	@GetMapping("/{fileId}")
	public ResponseEntity<String> downloadFile(
			@PathVariable Long courseId,
			@PathVariable Long assignmentId,
			@PathVariable Long fileId) {
		String downloadUrl = assignmentService.downloadFile(courseId, assignmentId, fileId);
		return ResponseEntity.ok(downloadUrl);
	}

	/**
	 * 특정 파일 삭제
	 */
	@DeleteMapping("/{fileId}")
	@PreAuthorize("hasRole('INSTRUCTOR')")
	public ResponseEntity<AssignmentResponseDto> deleteFile(
			@PathVariable Long courseId,
			@PathVariable Long assignmentId,
			@PathVariable Long fileId) {
		AssignmentResponseDto responseDto = assignmentService.deleteFile(courseId, assignmentId, fileId);
		return ResponseEntity.ok(responseDto);
	}

}
