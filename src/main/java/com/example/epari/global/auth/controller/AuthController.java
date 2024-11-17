package com.example.epari.global.auth.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.epari.global.auth.dto.ErrorResponse;
import com.example.epari.global.auth.dto.SignUpRequestDto;
import com.example.epari.global.auth.dto.VerificationRequestDto;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

	private final CognitoIdentityProviderClient cognitoClient;

	@Value("${aws.cognito.userPoolId}")
	private String userPoolId;

	@Value("${aws.cognito.clientId}")
	private String clientId;

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

	@PostMapping("/send-verification")
	public ResponseEntity<?> sendVerificationCode(@RequestBody VerificationRequestDto request) {
		try {
			String username = request.getEmail().split("@")[0];

			List<AttributeType> attributes = Arrays.asList(
					AttributeType.builder()
							.name("email")
							.value(request.getEmail())
							.build(),
					AttributeType.builder()
							.name("name")
							.value(username)
							.build()
			);

			SignUpRequest signUpRequest = SignUpRequest.builder()
					.clientId(clientId)
					.username(username)
					.password("TempPass123!")
					.userAttributes(attributes)
					.build();

			cognitoClient.signUp(signUpRequest);

			return ResponseEntity.ok().build();
		} catch (Exception e) {
			logger.error("Failed to send verification code", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorResponse("인증 코드 전송에 실패했습니다.: " + e.getMessage()));
		}
	}

	@PostMapping("/verify-code")
	public ResponseEntity<?> verifyCode(@RequestBody VerificationRequestDto request) {
		try {
			ConfirmSignUpRequest confirmRequest = ConfirmSignUpRequest.builder()
					.clientId(clientId)
					.username(request.getEmail().split("@")[0])
					.confirmationCode(request.getCode())
					.build();

			cognitoClient.confirmSignUp(confirmRequest);

			return ResponseEntity.ok().build();
		} catch (Exception e) {
			logger.error("Failed to verify code", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorResponse("인증 코드 확인에 실패했습니다."));
		}
	}

	@PostMapping("/signup")
	public ResponseEntity<?> signUp(@RequestBody SignUpRequestDto request) {
		try {
			// 1. 비밀번호 업데이트
			AdminSetUserPasswordRequest passwordRequest = AdminSetUserPasswordRequest.builder()
					.userPoolId(userPoolId)
					.username(request.getUsername())
					.password(request.getPassword())
					.permanent(true)
					.build();

			cognitoClient.adminSetUserPassword(passwordRequest);

			// 2. 사용자 속성 업데이트 (name만 업데이트)
			AdminUpdateUserAttributesRequest attributesRequest = AdminUpdateUserAttributesRequest.builder()
					.userPoolId(userPoolId)
					.username(request.getUsername())
					.userAttributes(
							AttributeType.builder()
									.name("name")
									.value(request.getName())
									.build()
					)
					.build();

			cognitoClient.adminUpdateUserAttributes(attributesRequest);

			// 3. PENDING_ROLES 그룹에 추가
			AdminAddUserToGroupRequest groupRequest = AdminAddUserToGroupRequest.builder()
					.userPoolId(userPoolId)
					.username(request.getUsername())
					.groupName("PENDING_ROLES")
					.build();

			cognitoClient.adminAddUserToGroup(groupRequest);

			return ResponseEntity.ok().build();
		} catch (Exception e) {
			logger.error("Failed to process signup", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorResponse("회원가입 처리 중 오류가 발생했습니다."));
		}
	}

}
