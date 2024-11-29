package com.example.epari.admin.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 강의 상세 정보 응답 DTO
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AdminCourseDetailResponseDto {

	private final Long id;

	private final String name;

	private final String classroom;

	private final InstructorInfo instructor;

	private final LocalDate startDate;

	private final LocalDate endDate;

	private final String imageUrl;

	private final int studentCount;

	private final List<CurriculumDetailDto> curriculums;

	public static AdminCourseDetailResponseDto of(Long id, String name, String classroom, Long instructorId,
			String instructorName, LocalDate startDate, LocalDate endDate, String imageUrl, int studentCount,
			List<CurriculumDetailDto> curriculums
	) {
		return new AdminCourseDetailResponseDto(
				id, name, classroom, new InstructorInfo(instructorId, instructorName),
				startDate, endDate, imageUrl, studentCount, curriculums
		);
	}

}
