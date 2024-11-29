package com.example.epari.admin.dto;

import java.time.LocalDate;

import lombok.Getter;

/**
 * 커리큘럼 상세 정보 응답 DTO
 */
@Getter
public class CurriculumDetailDto {

	private final Long id;

	private final LocalDate date;

	private final String topic;

	private final String description;

	public CurriculumDetailDto(Long id, LocalDate date, String topic, String description) {
		this.id = id;
		this.date = date;
		this.topic = topic;
		this.description = description;
	}

}
