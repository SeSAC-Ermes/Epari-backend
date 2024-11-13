package com.example.epari.board.dto;

import com.example.epari.board.domain.NoticeFile;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeFileDto {

	private Long id;

	private String originalFileName;

	private String fileUrl;

	private Long fileSize;

	@Builder
	public NoticeFileDto(Long id, String originalFileName, String fileUrl, Long fileSize) {
		this.id = id;
		this.originalFileName = originalFileName;
		this.fileUrl = fileUrl;
		this.fileSize = fileSize;
	}

	public static NoticeFileDto from(NoticeFile noticeFile) {
		return NoticeFileDto.builder()
				.id(noticeFile.getId())
				.originalFileName(noticeFile.getOriginalFileName())
				.fileUrl(noticeFile.getFileUrl())
				.fileSize(noticeFile.getFileSize())
				.build();
	}

}
