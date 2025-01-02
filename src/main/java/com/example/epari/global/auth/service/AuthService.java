package com.example.epari.global.auth.service;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import com.example.epari.global.auth.dto.SignUpRequestDto;
import com.example.epari.global.auth.dto.VerificationRequestDto;
import com.example.epari.global.common.base.BaseUser;
import com.example.epari.global.common.repository.BaseUserRepository;
import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;
import com.example.epari.global.exception.auth.*;
import com.example.epari.user.domain.ProfileImage;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

	private final CognitoIdentityProviderClient cognitoClient;

	private final BaseUserRepository userRepository;

	@Value("${aws.cognito.userpool.id}")
	private String userPoolId;

	@Value("${aws.cognito.clientId}")
	private String clientId;

	// ===== 회원가입 관련 =====
	public void signUp(SignUpRequestDto request) {
		try {
			UserType user = findUserByEmail(request.getEmail(), true);
			setUserPassword(user, request.getPassword());
			updateUserAttributes(user, request.getName());
			log.info("User signup completed successfully: {}", request.getEmail());
		} catch (Exception e) {
			log.error("Failed to process signup", e);
			throw new SignUpFailedException();
		}
	}

	private void setUserPassword(UserType user, String password) {
		AdminSetUserPasswordRequest request = AdminSetUserPasswordRequest.builder()
				.userPoolId(userPoolId)
				.username(user.username())
				.password(password)
				.permanent(true)
				.build();
		cognitoClient.adminSetUserPassword(request);
	}

	private void updateUserAttributes(UserType user, String name) {
		AdminUpdateUserAttributesRequest request = AdminUpdateUserAttributesRequest.builder()
				.userPoolId(userPoolId)
				.username(user.username())
				.userAttributes(
						AttributeType.builder()
								.name("name")
								.value(name)
								.build()
				)
				.build();
		cognitoClient.adminUpdateUserAttributes(request);
	}

	private void addUserToGroup(UserType user, String groupName) {
		AdminAddUserToGroupRequest request = AdminAddUserToGroupRequest.builder()
				.userPoolId(userPoolId)
				.username(user.username())
				.groupName(groupName)
				.build();
		cognitoClient.adminAddUserToGroup(request);
	}

	// ===== 이메일 인증 관련 =====
	public void sendVerificationCode(String email) {
		try {
			UserType user = findUserByEmail(email, false);
			if (user != null) {
				validateUserGroups(user);
				resendVerificationCode(user.username());
			} else {
				createNewUser(email);
			}
		} catch (BusinessBaseException e) {
			throw e;
		} catch (Exception e) {
			log.error("Failed to send verification code", e);
			throw new AuthenticationException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	private void validateUserGroups(UserType user) {
		AdminListGroupsForUserResponse groupsResponse = getUserGroups(user.username());
		if (isActiveUser(groupsResponse)) {
			throw new UserAlreadyExistsException();
		}
		if (isPendingUser(groupsResponse)) {
			throw new AuthenticationException(ErrorCode.PENDING_APPROVAL);
		}
	}

	public void resendVerificationCode(String username) {
		ResendConfirmationCodeRequest request = ResendConfirmationCodeRequest.builder()
				.clientId(clientId)
				.username(username)
				.build();
		cognitoClient.resendConfirmationCode(request);
	}

	public void verifyCode(VerificationRequestDto request) {
		try {
			UserType user = findUserByEmail(request.getEmail(), true);
			confirmSignUp(user.username(), request.getCode());
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

	private void confirmSignUp(String username, String code) {
		ConfirmSignUpRequest request = ConfirmSignUpRequest.builder()
				.clientId(clientId)
				.username(username)
				.confirmationCode(code)
				.build();
		cognitoClient.confirmSignUp(request);
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

			SignUpRequest request = SignUpRequest.builder()
					.clientId(clientId)
					.username(uuid)
					.userAttributes(attributes)
					.password("TempPass123!")
					.build();

			cognitoClient.signUp(request);
			log.info("New user created successfully with UUID: {}", uuid);
		} catch (UsernameExistsException e) {
			log.error("Username already exists");
			throw new UserAlreadyExistsException();
		} catch (Exception e) {
			log.error("Failed to create new user. Error: {}", e.getMessage());
			throw new SignUpFailedException();
		}
	}

	// ===== Google 로그인 관련 =====
	@Transactional
	public void updateGoogleProfileImage(String email, String imageUrl) {
		try {
			AdminListGroupsForUserResponse groupsResponse = getUserGroups(email);
			if (!isActiveUser(groupsResponse)) {
				log.error("Unauthorized user attempting to update profile: {}", email);
				throw new AuthenticationException(ErrorCode.UNAUTHORIZED);
			}

			BaseUser user = userRepository.findByEmail(email)
					.orElseThrow(AuthUserNotFoundException::new);
			user.updateProfileImage(ProfileImage.of(null, null, imageUrl, null));
			log.info("Successfully updated Google profile image for user: {}", email);
		} catch (Exception e) {
			log.error("Failed to update Google profile image for user: {}", email, e);
			throw new AuthenticationException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	// ===== 그룹 관련 =====
	public List<String> getUserGroupsList(String email) {
		try {
			AdminListGroupsForUserResponse groupsResponse = getUserGroups(email);
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

	// ===== 유틸리티 메소드 =====
	private UserType findUserByEmail(String email, boolean throwExceptionIfNotFound) {
		try {
			ListUsersRequest request = ListUsersRequest.builder()
					.userPoolId(userPoolId)
					.filter("email = \"" + email + "\" or username = \"" + email + "\"")
					.build();

			ListUsersResponse response = cognitoClient.listUsers(request);
			if (response.users().isEmpty()) {
				if (throwExceptionIfNotFound) {
					throw new AuthUserNotFoundException();
				}
				return null;
			}
			return response.users().get(0);
		} catch (BusinessBaseException e) {
			throw e;
		} catch (Exception e) {
			log.error("Failed to find user by email: {}", email, e);
			throw new AuthenticationException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	private AdminListGroupsForUserResponse getUserGroups(String email) {
		try {
			UserType user = findUserByEmail(email, true);
			return cognitoClient.adminListGroupsForUser(
					AdminListGroupsForUserRequest.builder()
							.userPoolId(userPoolId)
							.username(user.username())
							.build()
			);
		} catch (Exception e) {
			log.error("Failed to get user groups for email: {}", email, e);
			throw new AuthenticationException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	private boolean isActiveUser(AdminListGroupsForUserResponse groupsResponse) {
		return groupsResponse.groups().stream()
				.anyMatch(group -> "INSTRUCTOR".equals(group.groupName())
						|| "STUDENT".equals(group.groupName()));
	}

	private boolean isPendingUser(AdminListGroupsForUserResponse groupsResponse) {
		return groupsResponse.groups().stream()
				.anyMatch(group -> "PENDING_ROLES".equals(group.groupName()));
	}

}
