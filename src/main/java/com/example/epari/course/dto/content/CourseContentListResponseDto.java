package com.example.epari.course.dto.content;

import java.time.LocalDate;
import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 강의 컨텐츠 목록 조회 결과를 담는 응답 DTO
 * 무한 스크롤을 위한 커서 정보를 포함합니다.
 */

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseContentListResponseDto {

	private List<CourseContentResponseDto> contents;

	private boolean hasNext;

	private CursorInfo cursor;

	@Getter
	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	public static class CursorInfo {

		private Long id;

		private LocalDate date;

		private CursorInfo(Long id, LocalDate date) {
			this.id = id;
			this.date = date;
		}

		public static CursorInfo of(CourseContentResponseDto lastContent) {
			if (lastContent == null) {
				return null;
			}
			return new CursorInfo(lastContent.getId(), lastContent.getDate());
		}

	}

	private CourseContentListResponseDto(List<CourseContentResponseDto> contents, boolean hasNext) {
		this.contents = contents;
		this.hasNext = hasNext;
		this.cursor = contents.isEmpty() ? null : CursorInfo.of(contents.get(contents.size() - 1));
	}

	public static CourseContentListResponseDto of(List<CourseContentResponseDto> contents, int pageSize) {
		boolean hasNext = contents.size() > pageSize;
		if (hasNext) {
			contents = contents.subList(0, pageSize);
		}
		return new CourseContentListResponseDto(contents, hasNext);
	}

}
