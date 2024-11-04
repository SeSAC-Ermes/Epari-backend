package com.example.epari.exam.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionImage {

	@Column(name = "image_original_name")
	private String originalFileName;

	@Column(name = "image_stored_name")
	private String storedFileName;

	@Column(name = "image_url")
	private String fileUrl;

	@Column(name = "image_size")
	private Long fileSize;

	@Builder
	public QuestionImage(String originalFileName, String storedFileName, String fileUrl, Long fileSize) {
		this.originalFileName = originalFileName;
		this.storedFileName = storedFileName;
		this.fileUrl = fileUrl;
		this.fileSize = fileSize;
	}

}
