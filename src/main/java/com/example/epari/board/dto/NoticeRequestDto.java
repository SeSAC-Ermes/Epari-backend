package com.example.epari.board.dto;

import com.example.epari.global.common.enums.NoticeType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeRequestDto {

	private String title;

	private String content;

	private NoticeType type;

	private Long courseId;

	private Long instructorId;

	private List<MultipartFile> files = new ArrayList<>(); // 새로 추가할 파일들

	private List<Long> deleteFileIds = new ArrayList<>(); // 삭제할 파일 ID 목록

	@Builder
	public NoticeRequestDto(String title, String content, NoticeType type,
							Long courseId, Long instructorId,
							List<MultipartFile> files, List<Long> deleteFileIds) {
		this.title = title;
		this.content = content;
		this.type = type;
		this.courseId = courseId;
		this.instructorId = instructorId;
		this.files = files != null ? files : new ArrayList<>();
		this.deleteFileIds = deleteFileIds != null ? deleteFileIds : new ArrayList<>();
	}

	// Setter methods for file-related fields
	public void setFiles(List<MultipartFile> files) {
		this.files = files != null ? files : new ArrayList<>();
	}

	public void setDeleteFileIds(List<Long> deleteFileIds) {
		this.deleteFileIds = deleteFileIds != null ? deleteFileIds : new ArrayList<>();
	}

}
