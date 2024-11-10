package com.example.epari.course.dto.course;

import java.time.LocalDate;

import com.example.epari.course.domain.Course;
import com.example.epari.user.domain.Instructor;

import lombok.Builder;
import lombok.Getter;

/**
 * 강의 응답용 DTO 클래스
 * 강의 조회 결과를 클라이언트에 반환하기 위한 객체입니다.
 */
@Getter
@Builder
public class CourseResponseDto {

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

	public static CourseResponseDto from(Course course) {
		return CourseResponseDto.builder()
				.id(course.getId())
				.name(course.getName())
				.startDate(course.getStartDate())
				.endDate(course.getEndDate())
				.classroom(course.getClassroom())
				.instructor(InstructorInfo.from(course.getInstructor()))
				.build();
	}

}
