package com.example.epari.assignment.dto.file;

import com.example.epari.assignment.domain.AssignmentFile;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 과제 첨부파일 정보를 반환하기 위한 응답 DTO
 */
@Getter
@NoArgsConstructor
public class AssignmentFileResponseDto {

	private Long id;                    // 파일 ID

	private String originalFileName;    // 원본 파일명

	private String storedFileName;      // 저장된 파일명

	private String fileUrl;             // 파일 URL

	private Long fileSize;              // 파일 크기

	public AssignmentFileResponseDto(AssignmentFile file) {
		this.id = file.getId();
		this.originalFileName = file.getOriginalFileName();
		this.storedFileName = file.getStoredFileName();
		this.fileUrl = file.getFileUrl();
		this.fileSize = file.getFileSize();
	}

}
