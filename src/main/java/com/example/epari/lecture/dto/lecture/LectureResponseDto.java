package com.example.epari.lecture.dto.lecture;

import java.time.LocalDate;

import com.example.epari.lecture.domain.Lecture;
import com.example.epari.user.domain.Instructor;

import lombok.Builder;
import lombok.Getter;

/**
 * 강의 응답용 DTO 클래스
 * 강의 조회 결과를 클라이언트에 반환하기 위한 객체입니다.
 */
@Getter
@Builder
public class LectureResponseDto {

	private Long id;

	private String name;

	private LocalDate startDate;

	private LocalDate endDate;

	private String classroom;

	private InstructorInfo instructor;

	@Getter
	@Builder
	public static class InstructorInfo {

		private Long id;

		private String name;

		public static InstructorInfo from(Instructor instructor) {
			return InstructorInfo.builder()
					.id(instructor.getId())
					.name(instructor.getName())
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
