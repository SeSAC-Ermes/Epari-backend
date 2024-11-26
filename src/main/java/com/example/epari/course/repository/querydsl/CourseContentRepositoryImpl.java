package com.example.epari.course.repository.querydsl;

import java.util.List;

import org.springframework.util.StringUtils;

import com.example.epari.course.domain.CourseContent;
import com.example.epari.course.domain.QCourse;
import com.example.epari.course.domain.QCourseContent;
import com.example.epari.course.dto.content.CourseContentCursorDto;
import com.example.epari.course.dto.content.CourseContentSearchRequestDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

/**
 * 강의 컨텐츠 검색을 위한 QueryDSL 구현체
 */

@RequiredArgsConstructor
public class CourseContentRepositoryImpl implements CourseContentRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<CourseContent> findWithCursor(
			Long courseId,
			CourseContentCursorDto cursor,
			int pageSize) {

		QCourseContent content = QCourseContent.courseContent;
		QCourse course = QCourse.course;

		BooleanBuilder builder = new BooleanBuilder();

		// 기본 검색 조건
		builder.and(content.course.id.eq(courseId));

		// 커서 기반 페이징 조건
		if (cursor != null) {
			builder.and(
					content.date.lt(cursor.getDate())
							.or(content.date.eq(cursor.getDate())
									.and(content.id.lt(cursor.getId())))
			);
		}

		return queryFactory
				.selectFrom(content)
				.join(content.course, course).fetchJoin()
				.leftJoin(content.files).fetchJoin()
				.where(builder)
				.orderBy(
						content.date.desc(),
						content.id.desc()
				)
				.limit(pageSize)
				.fetch();
	}

	@Override
	public List<CourseContent> searchWithCursor(
			Long courseId,
			CourseContentSearchRequestDto searchRequest,
			CourseContentCursorDto cursor,
			int pageSize) {

		QCourseContent content = QCourseContent.courseContent;
		QCourse course = QCourse.course;

		BooleanBuilder builder = new BooleanBuilder();

		// 기본 검색 조건
		builder.and(content.course.id.eq(courseId));

		// 제목 검색
		if (StringUtils.hasText(searchRequest.getTitle())) {
			builder.and(content.title.contains(searchRequest.getTitle()));
		}

		// 내용 검색
		if (StringUtils.hasText(searchRequest.getContent())) {
			builder.and(content.content.contains(searchRequest.getContent()));
		}

		// 커서 기반 페이징 조건
		if (cursor != null) {
			builder.and(
					content.date.lt(cursor.getDate())
							.or(content.date.eq(cursor.getDate())
									.and(content.id.lt(cursor.getId())))
			);
		}

		return queryFactory
				.selectFrom(content)
				.join(content.course, course).fetchJoin()
				.leftJoin(content.files).fetchJoin()
				.where(builder)
				.orderBy(
						content.date.desc(),
						content.id.desc()
				)
				.limit(pageSize)
				.fetch();
	}

}
