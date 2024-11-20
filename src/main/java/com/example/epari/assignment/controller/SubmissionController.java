package com.example.epari.assignment.controller;

import com.example.epari.assignment.dto.submission.GradeRequestDto;
import com.example.epari.assignment.dto.submission.SubmissionRequestDto;
import com.example.epari.assignment.dto.submission.SubmissionResponseDto;
import com.example.epari.assignment.service.SubmissionService;
import com.example.epari.global.annotation.CurrentUserEmail;
import com.example.epari.user.domain.Student;
import com.example.epari.user.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

	@PreAuthorize("hasRole('STUDENT')")
	@GetMapping("/{submissionId}")
	public ResponseEntity<SubmissionResponseDto> getSubmission(
			@PathVariable Long courseId,
			@PathVariable Long assignmentId,
			@PathVariable Long submissionId) {
		return ResponseEntity.ok(
				submissionService.getSubmissionById(courseId, assignmentId, submissionId)
		);
	}

	@PreAuthorize("hasRole('STUDENT')")
	@GetMapping
	public ResponseEntity<SubmissionResponseDto> getStudentSubmission(
			@PathVariable Long courseId,
			@PathVariable Long assignmentId,
			@CurrentUserEmail String email) {
		Student student = studentRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("학생 정보를 찾을 수 없습니다."));

		SubmissionResponseDto submission = submissionService.getStudentSubmission(
				courseId, assignmentId, student.getId());

		// 제출물이 없는 경우 404 대신 200 OK와 null을 반환
		return ResponseEntity.ok(submission);
	}

	@PreAuthorize("hasRole('STUDENT')")
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

	@PreAuthorize("hasRole('INSTRUCTOR')")
	@PutMapping("/{submissionId}/grade")
	public ResponseEntity<SubmissionResponseDto> gradeSubmission(
			@PathVariable Long courseId,
			@PathVariable Long assignmentId,
			@PathVariable Long submissionId,
			@RequestBody GradeRequestDto gradeRequestDto) {
		return ResponseEntity.ok(
				submissionService.gradeSubmission(submissionId,
						gradeRequestDto.getGrade(),
						gradeRequestDto.getFeedback())
		);
	}

	@PreAuthorize("hasRole('STUDENT')")
	@DeleteMapping("/{submissionId}")
	public ResponseEntity<Void> deleteSubmission(
			@PathVariable Long courseId,
			@PathVariable Long assignmentId,
			@PathVariable Long submissionId,
			@CurrentUserEmail String email) {
		Student student = studentRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("학생 정보를 찾을 수 없습니다."));

		submissionService.deleteSubmission(courseId, assignmentId, submissionId, student.getId());
		return ResponseEntity.ok().build();
	}

}
