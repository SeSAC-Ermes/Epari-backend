package com.example.epari.exam.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.epari.exam.domain.ExamResult;

/**
 * 학생별 시험 결과를 조회하는 레포지토리 인터페이스 구현
 */

public interface ExamResultRepository extends JpaRepository<ExamResult, Long> {

	// 특정 학생의 모든 시험 결과 조회 (시험 정보와 점수 포함)
	@Query("""
			 SELECT DISTINCT er FROM ExamResult er
			 JOIN FETCH er.exam e
			 JOIN FETCH er.student s
			 WHERE e.course.id = :courseId
			 ORDER BY s.name ASC, e.examDateTime DESC
			""")
	List<ExamResult> findAllByCourseId(@Param("courseId") Long courseId);

}
