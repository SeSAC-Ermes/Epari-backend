package com.example.epari.assignment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.epari.assignment.dto.submission.SubmissionRequestDto;
import com.example.epari.assignment.dto.submission.SubmissionResponseDto;
import com.example.epari.assignment.service.SubmissionService;
import com.example.epari.global.annotation.CurrentUserEmail;
import com.example.epari.user.domain.Student;
import com.example.epari.user.repository.StudentRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/courses/{courseId}/assignments/{assignmentId}/submissions")

public class SubmissionController {

	private final SubmissionService submissionService;

	private final StudentRepository studentRepository; // 추가

	@PreAuthorize("hasRole('STUDENT')")
	@PostMapping
	public ResponseEntity<SubmissionResponseDto> addSubmission(
			@PathVariable Long courseId,
			@PathVariable Long assignmentId,
			@ModelAttribute SubmissionRequestDto requestDto,
			@CurrentUserEmail String email) {
		Student student = studentRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("학생 정보를 찾을 수 없습니다."));

		return ResponseEntity.ok(
				submissionService.addSubmission(courseId, assignmentId, requestDto, student.getId())
		);
	}

	@GetMapping("/{submissionId}")
	public ResponseEntity<SubmissionResponseDto> getSubmission(
			@PathVariable Long courseId,
			@PathVariable Long assignmentId,
			@PathVariable Long submissionId) {
		return ResponseEntity.ok(
				submissionService.getSubmissionById(courseId, assignmentId, submissionId)
		);
	}

	@PutMapping("/{submissionId}")
	public ResponseEntity<SubmissionResponseDto> updateSubmission(
			@PathVariable Long courseId,
			@PathVariable Long assignmentId,
			@PathVariable Long submissionId,
			@ModelAttribute SubmissionRequestDto requestDto,
			@CurrentUserEmail String email) {
		Student student = studentRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("학생 정보를 찾을 수 없습니다."));

		return ResponseEntity.ok(
				submissionService.updateSubmission(courseId, assignmentId, submissionId,
						requestDto, student.getId())
		);
	}

	@PutMapping("/{submissionId}/grade")
	public ResponseEntity<SubmissionResponseDto> gradeSubmission(
			@PathVariable Long submissionId,
			@RequestParam String grade,
			@RequestParam String feedback) {
		return ResponseEntity.ok(
				submissionService.gradeSubmission(submissionId, grade, feedback)
		);
	}

}
