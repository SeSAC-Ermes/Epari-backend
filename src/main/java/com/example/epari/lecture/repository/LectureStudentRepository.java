package com.example.epari.lecture.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.epari.lecture.domain.LectureStudent;

public interface LectureStudentRepository extends JpaRepository<LectureStudent, Long> {

	/**
	 * 특정 강의의 모든 수강생 조회
	 */
	@Query("""
			SELECT ls
			FROM LectureStudent ls
			JOIN FETCH ls.student
			WHERE ls.lecture.id = :lectureId
			""")
	List<LectureStudent> findAllLectureStudentsByLectureId(
			@Param("lectureId") Long lectureId
	);

}
