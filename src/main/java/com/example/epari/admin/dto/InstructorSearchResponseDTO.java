package com.example.epari.admin.dto;

import lombok.Getter;

/**
 * 강사 검색 결과를 담는 응답 DTO
 */
@Getter
public class InstructorSearchResponseDTO {

	private final Long id;

	private final String name;

	private final String email;

	public InstructorSearchResponseDTO(Long id, String name, String email) {
		this.id = id;
		this.name = name;
		this.email = email;
	}

}
