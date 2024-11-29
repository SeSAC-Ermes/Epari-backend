package com.example.epari.admin.dto;

import lombok.Getter;

/**
 * 강사 정보 응답 DTO
 */
@Getter
public class InstructorInfo {

	private final Long id;

	private final String name;

	public InstructorInfo(Long id, String name) {
		this.id = id;
		this.name = name;
	}

}
