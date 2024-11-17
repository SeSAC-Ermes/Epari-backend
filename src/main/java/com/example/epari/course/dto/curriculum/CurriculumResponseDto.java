package com.example.epari.course.dto.curriculum;

import java.time.LocalDate;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 커리큘럼 응답 정보를 담는 DTO 클래스 
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CurriculumResponseDto {

	private LocalDate date;

	private String topic;

	private String description;

	@Builder
	public CurriculumResponseDto(
			LocalDate date,
			String topic,
			String description
	) {
		this.date = date;
		this.topic = topic;
		this.description = description;
	}

}
