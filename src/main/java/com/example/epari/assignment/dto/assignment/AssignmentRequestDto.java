package com.example.epari.assignment.dto.assignment;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 과제 생성 및 수정을 위한 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class AssignmentRequestDto {

	private String title;           // 과제 제목

	private String description;     // 과제 설명

	private LocalDateTime deadline; // 마감기한
	
}
