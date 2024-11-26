package com.example.epari.course.dto.content;

import java.time.LocalDate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 커서 기반 페이징을 위한 DTO
 */

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseContentCursorDto {

	private Long id;

	private LocalDate date;

	public static CourseContentCursorDto of(Long id, LocalDate date) {
		CourseContentCursorDto dto = new CourseContentCursorDto();
		dto.id = id;
		dto.date = date;
		return dto;
	}

}
