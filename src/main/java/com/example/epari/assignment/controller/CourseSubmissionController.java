package com.example.epari.assignment.controller;

import com.example.epari.assignment.dto.submission.SubmissionResponseDto;
import com.example.epari.assignment.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/courses/{courseId}/submissions")
public class CourseSubmissionController {
	private final SubmissionService submissionService;

	/**
	 * 특정 강의의 모든 과제 제출물 조회
	 */
	@PreAuthorize("hasRole('INSTRUCTOR')")
	@GetMapping
	public ResponseEntity<List<SubmissionResponseDto>> getCourseSubmissions(
			@PathVariable Long courseId) {
		List<SubmissionResponseDto> submissions = submissionService.getSubmissionsByCourse(courseId);
		return ResponseEntity.ok(submissions);
	}
}
