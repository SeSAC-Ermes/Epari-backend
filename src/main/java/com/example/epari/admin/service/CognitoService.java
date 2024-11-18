package com.example.epari.admin.service;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.epari.admin.dto.CognitoUserDTO;
import com.example.epari.admin.exception.CognitoException;
import com.example.epari.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminAddUserToGroupRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminDeleteUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminRemoveUserFromGroupRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersInGroupRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersInGroupResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotFoundException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserType;

/**
 * Cognito 관련 비즈니스 로직을 처리하는 서비스 클래스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CognitoService {

	private final CognitoIdentityProviderClient cognitoClient;

	@Value("${aws.cognito.userpool.id}")
	private String userPoolId;

	/**
	 * 그룹 이름을 입력받아 해당 그룹에 속한 사용자를 반환
	 */
	public List<CognitoUserDTO> getUsersByGroup(String groupName) {
		try {
			// 1. 그룹에 속한 사용자 목록 조회
			ListUsersInGroupRequest listUsersRequest = ListUsersInGroupRequest.builder()
					.userPoolId(userPoolId)
					.groupName(groupName)
					.build();

			ListUsersInGroupResponse listUsersResponse =
					cognitoClient.listUsersInGroup(listUsersRequest);

			// 2. 사용자 정보를 DTO로 변환
			return listUsersResponse.users().stream()
					.map(this::convertToDTO)
					.collect(Collectors.toList());
		} catch (CognitoIdentityProviderException ex) {
			log.error("Failed to fetch users from Cognito group: {}", groupName, ex);
			throw new CognitoException(ErrorCode.COGNITO_USER_FETCH_ERROR);
		}
	}

	/**
	 * 사용자 그룹을 변경
	 */
	public void changeUserGroup(String email, String groupName) {
		// 1. 현재 그룹에서 제거
		AdminRemoveUserFromGroupRequest removeRequest = AdminRemoveUserFromGroupRequest.builder()
				.userPoolId(userPoolId)
				.username(email)
				.groupName("PENDING_ROLES")
				.build();

		cognitoClient.adminRemoveUserFromGroup(removeRequest);

		// 2. 새 그룹에 추가
		AdminAddUserToGroupRequest addRequest = AdminAddUserToGroupRequest.builder()
				.userPoolId(userPoolId)
				.username(email)
				.groupName(groupName)
				.build();

		cognitoClient.adminAddUserToGroup(addRequest);
	}

	/**
	 * 사용자 풀에서 특정 사용자를 삭제하는 메서드
	 */
	@Transactional
	public void deleteUser(String email) {
		try {
			// AdminDeleteUserRequest 생성
			AdminDeleteUserRequest deleteRequest = AdminDeleteUserRequest.builder()
					.userPoolId(userPoolId)
					.username(email)
					.build();

			// Cognito API를 통해 사용자 삭제 요청
			cognitoClient.adminDeleteUser(deleteRequest);

			log.info("Successfully deleted user from Cognito: {}", email);
		} catch (UserNotFoundException ex) {
			log.error("User not found in Cognito: {}", email, ex);
			throw new CognitoException(ErrorCode.COGNITO_USER_NOT_FOUND);
		} catch (CognitoIdentityProviderException ex) {
			log.error("Failed to delete user from Cognito: {}", email, ex);
			throw new CognitoException(ErrorCode.COGNITO_USER_DELETE_ERROR);
		}
	}

	private CognitoUserDTO convertToDTO(UserType user) {
		Map<String, String> attributes = user.attributes().stream()
				.collect(Collectors.toMap(
						AttributeType::name,
						AttributeType::value
				));

		return CognitoUserDTO.builder()
				.username(user.username())
				.email(getAttributeValue(user.attributes()))
				.status(user.userStatus().toString())
				.userCreateDate(user.userCreateDate().atZone(
						ZoneId.systemDefault()).toLocalDateTime())
				.attributes(attributes)
				.build();
	}

	private String getAttributeValue(List<AttributeType> attributes) {
		return attributes.stream()
				.filter(attr -> attr.name().equals("email"))
				.findFirst()
				.map(AttributeType::value)
				.orElse(null);
	}

}
