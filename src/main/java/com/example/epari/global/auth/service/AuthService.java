package com.example.epari.global.auth.service;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import com.example.epari.global.auth.dto.SignUpRequestDto;
import com.example.epari.global.auth.dto.VerificationRequestDto;
import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;
import com.example.epari.global.exception.auth.AuthUserNotFoundException;
import com.example.epari.global.exception.auth.AuthenticationException;
import com.example.epari.global.exception.auth.InvalidVerificationCodeException;
import com.example.epari.global.exception.auth.SignUpFailedException;
import com.example.epari.global.exception.auth.UserAlreadyExistsException;
import com.example.epari.global.exception.auth.VerificationCodeExpiredException;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 사용자 인증 관련 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

	private final CognitoIdentityProviderClient cognitoClient;

	@Value("${aws.cognito.userpool.id}")
	private String userPoolId;

	@Value("${aws.cognito.clientId}")
	private String clientId;

	public void signUp(SignUpRequestDto request) {
		try {
			UserType user = findUserByEmail(request.getEmail(), true);

			// 비밀번호 설정
			AdminSetUserPasswordRequest passwordRequest = AdminSetUserPasswordRequest.builder()
					.userPoolId(userPoolId)
					.username(user.username())
					.password(request.getPassword())
					.permanent(true)
					.build();

			cognitoClient.adminSetUserPassword(passwordRequest);

			// 사용자 속성 업데이트
			AdminUpdateUserAttributesRequest attributesRequest = AdminUpdateUserAttributesRequest.builder()
					.userPoolId(userPoolId)
					.username(user.username())
					.userAttributes(
							AttributeType.builder()
									.name("name")
									.value(request.getName())
									.build()
					)
					.build();

			cognitoClient.adminUpdateUserAttributes(attributesRequest);

			// PENDING_ROLES 그룹에 추가
			AdminAddUserToGroupRequest groupRequest = AdminAddUserToGroupRequest.builder()
					.userPoolId(userPoolId)
					.username(user.username())
					.groupName("PENDING_ROLES")
					.build();

			cognitoClient.adminAddUserToGroup(groupRequest);
			log.info("User signup completed successfully: {}", request.getEmail());

		} catch (Exception e) {
			log.error("Failed to process signup", e);
			throw new SignUpFailedException();
		}
	}

	public void sendVerificationCode(String email) {
		try {
			UserType user = findUserByEmail(email, false);

			if (user != null) {
				// 기존 사용자인 경우
				AdminListGroupsForUserRequest groupsRequest = AdminListGroupsForUserRequest.builder()
						.userPoolId(userPoolId)
						.username(user.username())
						.build();

				AdminListGroupsForUserResponse groupsResponse = cognitoClient.adminListGroupsForUser(groupsRequest);

				// 그룹 체크
				for (GroupType group : groupsResponse.groups()) {
					if (group.groupName().equals("INSTRUCTOR") || group.groupName().equals("STUDENT")) {
						throw new UserAlreadyExistsException();
					}
					if (group.groupName().equals("PENDING_ROLES")) {
						throw new AuthenticationException(ErrorCode.PENDING_APPROVAL);
					}
				}

				// 인증 코드 재발송
				ResendConfirmationCodeRequest resendRequest = ResendConfirmationCodeRequest.builder()
						.clientId(clientId)
						.username(user.username())
						.build();

				cognitoClient.resendConfirmationCode(resendRequest);
			} else {
				// 새 사용자 생성
				createNewUser(email);
			}
		} catch (BusinessBaseException e) {
			throw e;
		} catch (Exception e) {
			log.error("Failed to send verification code", e);
			throw new AuthenticationException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	public void verifyCode(VerificationRequestDto request) {
		try {
			UserType user = findUserByEmail(request.getEmail(), true);

			ConfirmSignUpRequest confirmRequest = ConfirmSignUpRequest.builder()
					.clientId(clientId)
					.username(user.username())
					.confirmationCode(request.getCode())
					.build();

			cognitoClient.confirmSignUp(confirmRequest);
			log.info("Email verified successfully: {}", request.getEmail());

		} catch (CodeMismatchException e) {
			throw new InvalidVerificationCodeException();
		} catch (ExpiredCodeException e) {
			throw new VerificationCodeExpiredException();
		} catch (Exception e) {
			log.error("Failed to verify code", e);
			throw new AuthenticationException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	public void resendVerificationCode(String email) {
		try {
			UserType user = findUserByEmail(email, true);

			ResendConfirmationCodeRequest resendRequest = ResendConfirmationCodeRequest.builder()
					.clientId(clientId)
					.username(user.username())
					.build();

			cognitoClient.resendConfirmationCode(resendRequest);
			log.info("Verification code resent for email: {}", email);

		} catch (LimitExceededException e) {
			throw new AuthenticationException(ErrorCode.VERIFICATION_CODE_LIMIT_EXCEEDED);
		} catch (Exception e) {
			log.error("Failed to resend verification code", e);
			throw new AuthenticationException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	private UserType findUserByEmail(String email, boolean throwExceptionIfNotFound) {
		try {
			// 이메일 필터 조건 수정
			ListUsersRequest listUsersRequest = ListUsersRequest.builder()
					.userPoolId(userPoolId)
					.filter("email = \"" + email + "\" or username = \"" + email + "\"")
					.build();

			ListUsersResponse listUsersResponse = cognitoClient.listUsers(listUsersRequest);

			if (listUsersResponse.users().isEmpty()) {
				if (throwExceptionIfNotFound) {
					throw new AuthUserNotFoundException();
				}
				return null;
			}

			return listUsersResponse.users().get(0);
		} catch (BusinessBaseException e) {
			throw e;
		} catch (Exception e) {
			log.error("Failed to find user by email: {}", email, e);
			throw new AuthenticationException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	private void createNewUser(String email) {
		try {
			String uuid = UUID.randomUUID().toString();
			log.info("Creating new user with UUID: {} for email: {}", uuid, email);

			List<AttributeType> attributes = Arrays.asList(
					AttributeType.builder()
							.name("email")
							.value(email)
							.build(),
					AttributeType.builder()
							.name("name")
							.value(email)
							.build()
			);

			SignUpRequest signUpRequest = SignUpRequest.builder()
					.clientId(clientId)
					.username(uuid)
					.userAttributes(attributes)
					.password("TempPass123!")
					.build();

			cognitoClient.signUp(signUpRequest);
			log.info("New user created successfully with UUID: {}", uuid);

		} catch (UsernameExistsException e) {
			log.error("Username already exists");
			throw new UserAlreadyExistsException();
		} catch (Exception e) {
			log.error("Failed to create new user. Error: {}", e.getMessage());
			throw new SignUpFailedException();
		}
	}

	public List<String> getUserGroups(String email) {
		try {
			UserType user = findUserByEmail(email, true); // 이미 구현된 메소드 활용

			AdminListGroupsForUserRequest groupsRequest = AdminListGroupsForUserRequest.builder()
					.userPoolId(userPoolId)
					.username(user.username())
					.build();

			AdminListGroupsForUserResponse groupsResponse = cognitoClient.adminListGroupsForUser(groupsRequest);

			return groupsResponse.groups().stream()
					.map(GroupType::groupName)
					.toList();

		} catch (UserNotFoundException e) {
			throw new AuthUserNotFoundException();
		} catch (Exception e) {
			log.error("Failed to get user groups", e);
			throw new AuthenticationException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	public void addUserToPendingRole(String email) {
		try {
			log.info("Attempting to add user to PENDING_ROLES group: {}", email);

			UserType user = findUserByEmail(email, true);
			log.info("Found user: {}", user.username());

			// 현재 사용자의 그룹 목록 확인
			AdminListGroupsForUserRequest listGroupsRequest = AdminListGroupsForUserRequest.builder()
					.userPoolId(userPoolId)
					.username(user.username())
					.build();

			AdminListGroupsForUserResponse groupsResponse =
					cognitoClient.adminListGroupsForUser(listGroupsRequest);

			// 이미 PENDING_ROLES 그룹에 있는지 확인
			boolean alreadyInPendingRole = groupsResponse.groups().stream()
					.anyMatch(group -> "PENDING_ROLES".equals(group.groupName()));

			if (!alreadyInPendingRole) {
				// PENDING_ROLES 그룹에 추가
				AdminAddUserToGroupRequest groupRequest = AdminAddUserToGroupRequest.builder()
						.userPoolId(userPoolId)
						.username(user.username())
						.groupName("PENDING_ROLES")
						.build();

				cognitoClient.adminAddUserToGroup(groupRequest);
				log.info("Successfully added user to PENDING_ROLES group: {}", email);
			} else {
				log.info("User already in PENDING_ROLES group: {}", email);
			}

		} catch (UserNotFoundException e) {
			log.error("User not found: {}", email);
			throw new AuthUserNotFoundException();
		} catch (CognitoIdentityProviderException e) {
			log.error("Cognito error while adding user to group: {}", e.getMessage());
			throw new AuthenticationException(ErrorCode.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			log.error("Unexpected error while adding user to PENDING_ROLES group", e);
			throw new AuthenticationException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

}
