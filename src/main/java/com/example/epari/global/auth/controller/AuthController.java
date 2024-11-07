package com.example.epari.global.auth.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

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
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
	}

}
