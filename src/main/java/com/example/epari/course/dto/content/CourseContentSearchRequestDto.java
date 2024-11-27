package com.example.epari.course.dto.content;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 강의 컨텐츠 검색을 위한 요청 DTO
 */

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseContentSearchRequestDto {

	private String title;

	private String content;

}
