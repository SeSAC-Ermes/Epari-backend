package com.example.epari.course.repository.querydsl;

import java.util.List;

import com.example.epari.course.domain.CourseContent;
import com.example.epari.course.dto.content.CourseContentCursorDto;
import com.example.epari.course.dto.content.CourseContentSearchRequestDto;

/**
 * 강의 컨텐츠 검색을 위한 커스텀 리포지토리 인터페이스
 */

public interface CourseContentRepositoryCustom {

	/**
	 * 강의 자료 제목/내용 검색 + 커서 기반 페이징
	 */

	List<CourseContent> searchWithCursor(
			Long courseId,
			CourseContentSearchRequestDto courseContentSearchRequest,
			CourseContentCursorDto cursor,
			int pageSize
	);

	/**
	 * 강의 자료 목록 조회 + 커서 기반 페이징
	 */

	List<CourseContent> findWithCursor(
			Long courseId,
			CourseContentCursorDto cursor,
			int pageSize
	);

}
