package com.example.epari.exam.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.epari.exam.domain.ExamResult;
import com.example.epari.global.common.enums.ExamStatus;

/**
 * 학생별 시험 결과를 조회하는 레포지토리 인터페이스 구현
 * 시험 정보에 대한 데이터베이스 접근을 담당하는 Repository 인터페이스
 */
public interface ExamResultRepository extends JpaRepository<ExamResult, Long> {

	// 특정 학생의 특정 시험 결과 조회
	@Query("SELECT er FROM ExamResult er WHERE er.exam.id = :examId AND er.student.id = :studentId")
	Optional<ExamResult> findByExamIdAndStudentId(
			@Param("examId") Long examId,
			@Param("studentId") Long studentId
	);

	// 특정 시험의 모든 결과 조회
	@Query("SELECT er FROM ExamResult er WHERE er.exam.id = :examId")
	List<ExamResult> findByExamId(@Param("examId") Long examId);

	//TODO: 특정 강의의 특정 학생 시험 결과 모두 조회
	//특정 강의의 특정 학생 시험 결과 모두 조회
	@Query("SELECT er FROM ExamResult er " +
			"WHERE er.exam.course.id = :courseId " +
			"AND er.student.id = :studentId")
	List<ExamResult> findByCourseIdAndStudentId(
			@Param("courseId") Long courseId,
			@Param("studentId") Long studentId
	);

	// 특정 상태의 시험 결과 조회
	@Query("SELECT er FROM ExamResult er WHERE er.exam.id = :examId AND er.status = :status")
	List<ExamResult> findByExamIdAndStatus(
			@Param("examId") Long examId,
			@Param("status") ExamStatus status
	);

	// 시간 초과된 진행중 시험 조회 (자동 제출용)
	@Query("""
			SELECT er FROM ExamResult er
			JOIN FETCH er.exam e
			WHERE er.status = 'IN_PROGRESS'
			AND e.examDateTime <= :baseTime
			""")
	List<ExamResult> findExpiredExams(@Param("baseTime") LocalDateTime baseTime);

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
