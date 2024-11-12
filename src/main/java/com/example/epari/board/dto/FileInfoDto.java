package com.example.epari.board.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileInfoDto {

	private String originalFileName;  // 원본 파일명

	private String storedFileName;    // 저장된 파일명

	private long fileSize;            // 파일 크기

	private String fileUrl;           // 파일 접근 URL

}
