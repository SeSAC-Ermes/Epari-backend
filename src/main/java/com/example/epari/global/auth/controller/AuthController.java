package com.example.epari.global.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GetUserResponse;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final CognitoIdentityProviderClient cognitoClient;

	@PostMapping("/validate")
	public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
		try {
			String jwtToken = token.replace("Bearer ", "");

			GetUserRequest getUserRequest = GetUserRequest.builder()
					.accessToken(jwtToken)
					.build();

			GetUserResponse userResult = cognitoClient.getUser(getUserRequest);

			return ResponseEntity.ok().build();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
	}

}
