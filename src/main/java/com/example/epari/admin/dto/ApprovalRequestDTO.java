package com.example.epari.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사용자 승인 요청 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApprovalRequestDTO {

	private String username; // Cognito username

	private Long courseId;

	private String name; // Username

}
