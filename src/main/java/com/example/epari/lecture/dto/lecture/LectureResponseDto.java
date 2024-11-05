package com.example.epari.lecture.dto.lecture;

import java.time.LocalDate;

import com.example.epari.lecture.domain.Lecture;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LectureResponseDto {

	private Long id;

	private String name;

	private LocalDate startDate;

	private LocalDate endDate;

	private String classroom;

	public static LectureResponseDto from(Lecture lecture) {
		return LectureResponseDto.builder()
				.id(lecture.getId())
				.name(lecture.getName())
				.startDate(lecture.getStartDate())
				.endDate(lecture.getEndDate())
				.classroom(lecture.getClassroom())
				.build();
	}

}
