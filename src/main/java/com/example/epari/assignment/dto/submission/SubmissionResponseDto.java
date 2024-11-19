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

	private String grade;           // 추가

	private String feedback;        // 추가

	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	private List<SubmissionFileResponseDto> files;

	private StudentInfo student;

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
					.phoneNumber(student.getPhoneNumber())
					.build();
		}

	}

	public static SubmissionResponseDto from(Submission submission) {
		return SubmissionResponseDto.builder()
				.id(submission.getId())
				.description(submission.getDescription())
				.grade(submission.getGrade())        // 추가
				.feedback(submission.getFeedback())  // 추가
				.createdAt(submission.getCreatedAt())
				.updatedAt(submission.getUpdatedAt())
				.files(submission.getFiles().stream()
						.map(SubmissionFileResponseDto::new)
						.collect(Collectors.toList()))
				.student(submission.getStudent() != null ?
						StudentInfo.from(submission.getStudent()) : null)
				.build();
	}

}