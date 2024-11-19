package com.example.epari.global.auth.dto;

import lombok.Data;

@Data
public class SignUpRequestDto {
	private String username;
	private String email;
	private String password;
	private String name;
	private String verificationCode;
}
