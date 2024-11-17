package com.example.epari.global.auth.dto;

import lombok.Data;

@Data
public class VerificationRequestDto {
    private String email;
    private String code;
}
