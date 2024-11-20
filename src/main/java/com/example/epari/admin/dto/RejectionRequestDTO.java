package com.example.epari.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사용자 반려 요청 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RejectionRequestDTO {

	private String username; // Cognito username

	private String name;    // Username

	private String reason;  // 반려 사유

}
