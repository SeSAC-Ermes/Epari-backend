package com.example.epari.admin.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 학생 정보를 담는 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseStudentResponseDTO {

	private Long id;

	private String name;

	private String email;

	private LocalDateTime createdAt;

}
