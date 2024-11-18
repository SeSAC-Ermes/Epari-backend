package com.example.epari.global.auth.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.epari.global.auth.dto.*;
import com.example.epari.global.auth.service.AuthService;
import com.example.epari.global.exception.ErrorCode;
import com.example.epari.global.exception.auth.AuthenticationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/validate")
	public ResponseEntity<?> validateToken(@AuthenticationPrincipal Jwt jwt) {
		try {
			String username = jwt.getSubject();
			List<String> groups = jwt.getClaimAsStringList("cognito:groups");

			Map<String, Object> response = new HashMap<>();
			response.put("username", username);
			response.put("groups", groups);

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("Token validation failed", e);
			throw new AuthenticationException(ErrorCode.UNAUTHORIZED);
		}
	}

	@PostMapping("/send-verification")
	public ResponseEntity<?> sendVerificationCode(@RequestBody VerificationRequestDto request) {
		authService.sendVerificationCode(request.getEmail());
		return ResponseEntity.ok().body(new SuccessResponseDto("인증 코드가 발송되었습니다."));
	}

	@PostMapping("/verify-code")
	public ResponseEntity<?> verifyCode(@RequestBody VerificationRequestDto request) {
		authService.verifyCode(request);
		return ResponseEntity.ok().body(new SuccessResponseDto("인증이 완료되었습니다."));
	}

	@PostMapping("/resend-verification")
	public ResponseEntity<?> resendVerificationCode(@RequestBody VerificationRequestDto request) {
		authService.resendVerificationCode(request.getEmail());
		return ResponseEntity.ok().body(new SuccessResponseDto("인증 코드가 재발송되었습니다."));
	}

	@PostMapping("/signup")
	public ResponseEntity<?> signUp(@RequestBody SignUpRequestDto request) {
		authService.signUp(request);
		return ResponseEntity.ok().body(new SuccessResponseDto("회원가입이 완료되었습니다."));
	}

}
