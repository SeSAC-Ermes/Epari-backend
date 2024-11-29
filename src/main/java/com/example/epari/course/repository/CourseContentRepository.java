package com.example.epari.course.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.epari.course.domain.CourseContent;
import com.example.epari.course.repository.querydsl.CourseContentRepositoryCustom;

/**
 * 강의 컨텐츠 엔티티에 대한 데이터베이스 작업을 처리하는 리포지토리
 */
public interface CourseContentRepository extends JpaRepository<CourseContent, Long>, CourseContentRepositoryCustom {

	// 특정 강의의 모든 강의 자료 조회
	@Query("SELECT lc FROM CourseContent lc WHERE lc.course.id = :courseId ORDER BY lc.date DESC")
	List<CourseContent> findAllByCourseId(@Param("courseId") Long courseId);

	// 특정 강의의 특정 강의 자료 조회
	@Query("SELECT lc FROM CourseContent lc WHERE lc.id = :contentId AND lc.course.id = :courseId")
	Optional<CourseContent> findByIdAndCourseId(
			@Param("contentId") Long contentId,
			@Param("courseId") Long courseId
	);

	// 특정 날짜의 강의 자료 조회
	@Query("SELECT lc FROM CourseContent lc WHERE lc.course.id = :courseId AND lc.date = :date")
	List<CourseContent> findByCourseIdAndDate(
			@Param("courseId") Long courseId,
			@Param("date") LocalDate date
	);

	/**
	 * 테스트 오프셋 기반 페이지네이션 쿼리
	 */
	@Query("SELECT c FROM CourseContent c WHERE c.course.id = :courseId")
	Page<CourseContent> findAllByCourseIdWithOffset(
			@Param("courseId") Long courseId,
			Pageable pageable);

}
