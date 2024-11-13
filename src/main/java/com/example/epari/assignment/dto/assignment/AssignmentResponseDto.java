package com.example.epari.assignment.dto.assignment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.example.epari.assignment.domain.Assignment;
import com.example.epari.assignment.dto.file.AssignmentFileResponseDto;
import com.example.epari.user.domain.Instructor;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AssignmentResponseDto {

	private Long id;

	private String title;

	private String description;

	private LocalDateTime deadline;

	private LocalDateTime createdAt;

	private List<AssignmentFileResponseDto> files;

	private InstructorInfo instructor;

	@Getter
	@Builder
	public static class InstructorInfo {

		private Long id;

		private String name;

		private String email;

		private String phoneNumber;

		public static InstructorInfo from(Instructor instructor) {
			return InstructorInfo.builder()
					.id(instructor.getId())
					.name(instructor.getName())
					.email(instructor.getEmail())
					.phoneNumber(instructor.getPhoneNumber())
					.build();
		}

	}

	public static AssignmentResponseDto from(Assignment assignment) {
		return AssignmentResponseDto.builder()
				.id(assignment.getId())
				.title(assignment.getTitle())
				.description(assignment.getDescription())
				.deadline(assignment.getDeadline())
				.createdAt(assignment.getCreatedAt())
				.files(assignment.getFiles().stream()
						.map(AssignmentFileResponseDto::new)
						.collect(Collectors.toList()))
				.instructor(assignment.getInstructor() != null ?
						InstructorInfo.from(assignment.getInstructor()) : null)
				.build();
	}

}
