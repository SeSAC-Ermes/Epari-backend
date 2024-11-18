package com.example.epari.admin.dto;

import lombok.Getter;

/**
 * 강의 검색 결과를 담는 응답 DTO
 */
@Getter
public class CourseSearchResponseDTO {

	private final Long id;

	private final String name;

	private final String instructor;

	public CourseSearchResponseDTO(Long id, String name, String instructor) {
		this.id = id;
		this.name = name;
		this.instructor = instructor;
	}

}
