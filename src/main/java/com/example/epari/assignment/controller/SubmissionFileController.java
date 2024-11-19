package com.example.epari.assignment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.epari.assignment.dto.submission.SubmissionResponseDto;
import com.example.epari.assignment.service.SubmissionService;

import lombok.RequiredArgsConstructor;

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
