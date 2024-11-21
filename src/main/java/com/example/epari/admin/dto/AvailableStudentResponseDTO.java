package com.example.epari.admin.dto;

import lombok.Getter;

/**
 * 강의에 등록 가능한 학생 정보를 담는 DTO
 */
@Getter
public class AvailableStudentResponseDTO {

	private final Long id;

	private final String name;

	private final String email;

	public AvailableStudentResponseDTO(Long id, String name, String email) {
		this.id = id;
		this.name = name;
		this.email = email;
	}

}
