package com.example.epari.user.service;

import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.epari.global.common.base.BaseUser;
import com.example.epari.global.common.repository.BaseUserRepository;
import com.example.epari.global.common.service.S3FileService;
import com.example.epari.user.domain.ProfileImage;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GetUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UpdateUserAttributesRequest;

@Service
@RequiredArgsConstructor
public class UserProfileService {

	private final S3FileService s3FileService;

	private final CognitoIdentityProviderClient cognitoClient;

	private final AsyncTaskExecutor asyncTaskExecutor;

	private final BaseUserRepository userRepository;

	@Transactional
	public String updateProfileImage(String username, MultipartFile file, String accessToken) {
		try {
			// 1. 현재 프로필 이미지 URL 가져오기
			String currentImageUrl = getCurrentProfileImageUrl(username, accessToken);

			// 2. 새 이미지 업로드
			String newImageUrl = s3FileService.uploadFile("profile-images", file);

			// 3. Cognito 사용자 속성 업데이트
			updateCognitoAttribute(accessToken, newImageUrl);

			// 4. DB 업데이트 - URL만 저장
			BaseUser user = userRepository.findByEmail(username)
					.orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
			ProfileImage profileImage = ProfileImage.of(
					null, null, newImageUrl, null
			);
			user.updateProfileImage(profileImage);

			// 5. 이전 이미지 비동기 삭제
			if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
				asyncTaskExecutor.execute(() -> {
					try {
						s3FileService.deleteFile(currentImageUrl);
					} catch (Exception e) {
						// 삭제 실패 로깅
					}
				});
			}

			return newImageUrl;
		} catch (Exception e) {
			throw new RuntimeException("프로필 이미지 업데이트 중 오류가 발생했습니다: " + e.getMessage(), e);
		}
	}

	private String getCurrentProfileImageUrl(String username, String accessToken) {
		try {
			GetUserResponse userResponse = cognitoClient.getUser(
					GetUserRequest.builder()
							.accessToken(accessToken)
							.build()
			);

			return userResponse.userAttributes().stream()
					.filter(attr -> attr.name().equals("custom:profile_image"))
					.findFirst()
					.map(AttributeType::value)
					.orElse(null);
		} catch (Exception e) {
			return null;
		}
	}

	private void updateCognitoAttribute(String accessToken, String imageUrl) {
		UpdateUserAttributesRequest updateRequest = UpdateUserAttributesRequest.builder()
				.accessToken(accessToken)
				.userAttributes(
						AttributeType.builder()
								.name("custom:profile_image")
								.value(imageUrl != null ? imageUrl : "")
								.build()
				)
				.build();

		cognitoClient.updateUserAttributes(updateRequest);
	}

	@Transactional
	public void deleteProfileImage(String username, String accessToken) {
		String currentImageUrl = getCurrentProfileImageUrl(username, accessToken);

		if (currentImageUrl != null) {
			// 1. Cognito 속성 먼저 업데이트
			updateCognitoAttribute(accessToken, "");

			// 2. DB 업데이트
			BaseUser user = userRepository.findByEmail(username)
					.orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
			user.updateProfileImage(null);

			// 3. S3 이미지 비동기 삭제
			asyncTaskExecutor.execute(() -> {
				try {
					s3FileService.deleteFile(currentImageUrl);
				} catch (Exception e) {
				}
			});
		}
	}

}
