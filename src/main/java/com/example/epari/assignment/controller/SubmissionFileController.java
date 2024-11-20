package com.example.epari.assignment.controller;

import com.example.epari.assignment.dto.submission.SubmissionResponseDto;
import com.example.epari.assignment.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/courses/{courseId}/assignments/{assignmentId}/submissions/{submissionId}/files")
public class SubmissionFileController {

	private final SubmissionService submissionService;

	@GetMapping("/{fileId}/download")
	public ResponseEntity<String> downloadFile(
			@PathVariable Long courseId,
			@PathVariable Long assignmentId,
			@PathVariable Long submissionId,
			@PathVariable Long fileId) {
		String presignedUrl = submissionService.downloadFile(courseId, assignmentId, submissionId, fileId);
		return ResponseEntity.ok(presignedUrl);
	}

	@DeleteMapping("/{fileId}")
	public ResponseEntity<SubmissionResponseDto> deleteFile(
			@PathVariable Long courseId,
			@PathVariable Long assignmentId,
			@PathVariable Long submissionId,
			@PathVariable Long fileId) {
		return ResponseEntity.ok(
				submissionService.deleteFile(courseId, assignmentId, submissionId, fileId)
		);
	}

}
