package com.example.epari.lecture.dto;

import java.time.LocalDate;

import com.example.epari.lecture.domain.Lecture;
import com.example.epari.user.domain.Instructor;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LectureResponseDto {
	// 강의 정보
	private Long id;
	private String name;
	private LocalDate startDate;
	private LocalDate endDate;
	private String classroom;

	// 강사 정보
	private InstructorInfo instructor;

	@Getter
	@Builder
	public static class InstructorInfo {
		private Long id;
		private String name;
		private String email;
		private String phoneNumber;
		private String careerHistory;

		public static InstructorInfo from(Instructor instructor) {
			return InstructorInfo.builder()
					.id(instructor.getId())
					.name(instructor.getName())
					.email(instructor.getEmail())
					.phoneNumber(instructor.getPhoneNumber())
					.careerHistory(instructor.getCareerHistory())
					.build();
		}
	}

	public static LectureResponseDto from(Lecture lecture) {
		return LectureResponseDto.builder()
				.id(lecture.getId())
				.name(lecture.getName())
				.startDate(lecture.getStartDate())
				.endDate(lecture.getEndDate())
				.classroom(lecture.getClassroom())
				.instructor(InstructorInfo.from(lecture.getInstructor()))
				.build();
	}
}
