package com.example.epari.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 프로필 이미지 정보를 관리하는 임베디드 타입
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProfileImage {

	@Column(name = "profile_original_file_name")
	private String originalFileName;

	@Column(name = "profile_stored_file_name")
	private String storedFileName;

	@Column(name = "profile_file_url")
	private String fileUrl;

	@Column(name = "profile_file_size")
	private Long fileSize;

	@Builder(access = AccessLevel.PRIVATE)    // 사용 이유 : 가독성이 좋고 유연한 객체 생성, 불변성 보장
	private ProfileImage(String originalFileName, String storedFileName, String fileUrl, Long fileSize) {
		this.originalFileName = originalFileName;
		this.storedFileName = storedFileName;
		this.fileUrl = fileUrl;
		this.fileSize = fileSize;
	}

	public static ProfileImage of(String originalFileName, String storedFileName, String fileUrl, Long fileSize) {
		return ProfileImage.builder()
				.originalFileName(originalFileName)
				.storedFileName(storedFileName)
				.fileUrl(fileUrl)
				.fileSize(fileSize)
				.build();
	}

}
