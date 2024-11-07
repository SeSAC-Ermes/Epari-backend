package com.example.epari.lecture.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.epari.lecture.domain.LectureContent;

/**
 * 강의 컨텐츠 엔티티에 대한 데이터베이스 작업을 처리하는 리포지토리
 */
public interface LectureContentRepository extends JpaRepository<LectureContent, Long> {

	// 특정 강의의 모든 강의 자료 조회
	@Query("SELECT lc FROM LectureContent lc WHERE lc.lecture.id = :lectureId ORDER BY lc.date DESC")
	List<LectureContent> findAllByLectureId(@Param("lectureId") Long lectureId);

	// 특정 강의의 특정 강의 자료 조회
	@Query("SELECT lc FROM LectureContent lc WHERE lc.id = :contentId AND lc.lecture.id = :lectureId")
	Optional<LectureContent> findByIdAndLectureId(
			@Param("contentId") Long contentId,
			@Param("lectureId") Long lectureId
	);

	// 특정 날짜의 강의 자료 조회
	@Query("SELECT lc FROM LectureContent lc WHERE lc.lecture.id = :lectureId AND lc.date = :date")
	List<LectureContent> findByLectureIdAndDate(
			@Param("lectureId") Long lectureId,
			@Param("date") LocalDate date
	);

	// 제목으로 강의 자료 검색
	@Query("SELECT lc FROM LectureContent lc " +
		   "WHERE lc.lecture.id = :lectureId " +
		   "AND lc.title LIKE %:keyword% " +
		   "ORDER BY lc.date DESC")
	List<LectureContent> findByLectureIdAndTitleContaining(
			@Param("lectureId") Long lectureId,
			@Param("keyword") String keyword
	);

}
