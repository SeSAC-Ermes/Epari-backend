package com.example.epari.assignment.dto.submission;

import com.example.epari.assignment.domain.Submission;
import com.example.epari.assignment.dto.file.SubmissionFileResponseDto;
import com.example.epari.user.domain.Student;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class SubmissionResponseDto {

	private Long id;

	private String description;

	private String grade;

	private String feedback;

	private String status;

	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	private List<SubmissionFileResponseDto> files;

	private StudentInfo student;

	private AssignmentInfo assignment;

	@Getter
	@Builder
	public static class StudentInfo {

		private Long id;

		private String name;

		private String email;

		private String phoneNumber;

		public static StudentInfo from(Student student) {
			return StudentInfo.builder()
					.id(student.getId())
					.name(student.getName())
					.email(student.getEmail())
					.build();
		}

	}

	@Getter
	@Builder
	public static class AssignmentInfo {

		private Long id;

		private String title;

		private LocalDateTime deadline;

		public static AssignmentInfo from(com.example.epari.assignment.domain.Assignment assignment) {
			return AssignmentInfo.builder()
					.id(assignment.getId())
					.title(assignment.getTitle())
					.deadline(assignment.getDeadline())
					.build();
		}

	}

	public static SubmissionResponseDto from(Submission submission) {
		return SubmissionResponseDto.builder()
				.id(submission.getId())
				.description(submission.getDescription())
				.grade(submission.getGrade() != null ? submission.getGrade().getDescription() : null)
				.feedback(submission.getFeedback())
				.status(submission.getStatus().getDescription())
				.createdAt(submission.getCreatedAt())
				.updatedAt(submission.getUpdatedAt())
				.files(submission.getFiles().stream()
						.map(SubmissionFileResponseDto::new)
						.collect(Collectors.toList()))
				.student(submission.getStudent() != null ?
						StudentInfo.from(submission.getStudent()) : null)
				.assignment(AssignmentInfo.from(submission.getAssignment()))
				.build();
	}

	public static SubmissionResponseDto createUnsubmitted(Student student, com.example.epari.assignment.domain.Assignment assignment) {
		return SubmissionResponseDto.builder()
				.description("")
				.grade(null)
				.feedback(null)
				.status("미제출")
				.files(List.of())
				.student(StudentInfo.from(student))
				.assignment(AssignmentInfo.from(assignment))
				.build();
	}
}
