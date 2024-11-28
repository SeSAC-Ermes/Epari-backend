package com.example.epari.exam.dto.response;

import com.example.epari.exam.domain.QuestionImage;

import lombok.Builder;
import lombok.Getter;

/**
 * 문제 이미지 응답 DTO
 */
@Getter
@Builder
class QuestionImageResponseDto {

	private String originalFileName;

	private String storedFileName;

	private String fileUrl;

	private Long fileSize;

	public static QuestionImageResponseDto from(QuestionImage image) {
		if (image == null) {
			return null;
		}

		return QuestionImageResponseDto.builder()
				.originalFileName(image.getOriginalFileName())
				.storedFileName(image.getStoredFileName())
				.fileUrl(image.getFileUrl())
				.fileSize(image.getFileSize())
				.build();
	}

}
