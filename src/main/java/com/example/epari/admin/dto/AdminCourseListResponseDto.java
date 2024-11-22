package com.example.epari.admin.dto;

import java.time.LocalDate;

import lombok.Getter;

/**
 * 강의 정보 목록을 담는 응답 DTO
 */
@Getter
public class AdminCourseListResponseDto {

	private final Long id;

	private final String name;

	private final String classroom;

	private final InstructorInfo instructor;

	private final LocalDate startDate;

	private final LocalDate endDate;

	private final int studentCount;

	public AdminCourseListResponseDto(Long id, String name, String classroom,
			Long instructorId, String instructorName,
			LocalDate startDate, LocalDate endDate, Long studentCount) {
		this.id = id;
		this.name = name;
		this.classroom = classroom;
		this.instructor = new InstructorInfo(instructorId, instructorName);
		this.startDate = startDate;
		this.endDate = endDate;
		this.studentCount = studentCount.intValue();
	}

	@Getter
	public static class InstructorInfo {

		private final Long id;

		private final String name;

		public InstructorInfo(Long id, String name) {
			this.id = id;
			this.name = name;
		}

	}

}
