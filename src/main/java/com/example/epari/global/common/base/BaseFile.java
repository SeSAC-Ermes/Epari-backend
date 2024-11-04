package com.example.epari.global.common.base;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 파일 확장자나 MIME 타입을 저장하는 필드를 추가 고려 :
 * 파일 유형을 쉽게 식별,
 * 다운로드 시 올바른 Content-Type 헤더 설정,
 * 특정 파일 형식만 허용하는 등의 검증이 용이
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "file_type")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseFile extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// 원본 파일명
	@Column(name = "original_file_name", nullable = false)
	private String originalFileName;

	// 저장된 파일명
	@Column(name = "stored_file_name", nullable = false)
	private String storedFileName;

	// 파일 접근 URL
	@Column(name = "file_url", nullable = false)
	private String fileUrl;

	// 파일 크기
	@Column(name = "file_size")
	private Long fileSize;

	protected BaseFile(String originalFileName, String storedFileName, String fileUrl, Long fileSize) {
		this.originalFileName = originalFileName;
		this.storedFileName = storedFileName;
		this.fileUrl = fileUrl;
		this.fileSize = fileSize;
	}

}
