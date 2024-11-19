package com.example.epari.admin.dto;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;

/**
 * Cognito 사용자 정보를 담는 DTO
 */
@Getter
@Builder
public class CognitoUserDTO {

	private String username;

	private String email;

	private String status;

	private LocalDateTime userCreateDate;

	private Map<String, String> attributes;

}
