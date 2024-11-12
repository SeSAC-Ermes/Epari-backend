package com.example.epari.board.dto;


import com.example.epari.global.common.enums.NoticeType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

// NoticeRequestDto : 공지사항 등록 Dto
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeRequestDto {

	private String title;

	private String content;

	private NoticeType type;

	private Long courseId;

	private Long instructorId;

	private List<MultipartFile> files; // 파일 업로드를 위한 필드

	private List<Long> deleteFileIds; // 삭제할 파일 ID 목록

	@Builder
	public NoticeRequestDto(String title, String content, NoticeType type,
							Long courseId, Long instructorId, List<MultipartFile> files) {
		this.title = title;
		this.content = content;
		this.type = type;
		this.courseId = courseId;
		this.instructorId = instructorId;
		this.files = files;
	}

}
