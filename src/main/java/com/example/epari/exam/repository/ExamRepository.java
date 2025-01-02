package com.example.epari.exam.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.epari.exam.domain.Exam;
import com.example.epari.global.common.enums.ExamStatus;

/**
 * 시험 정보에 대한 데이터베이스 접근을 담당하는 Repository 인터페이스
 */
public interface ExamRepository extends JpaRepository<Exam, Long> {

	// 특정 강의에 속한 모든 시험 조회
	List<Exam> findByCourseId(Long courseId);

	// 특정 강의에 속한 시험 상세 조회
	Optional<Exam> findByCourseIdAndId(Long courseId, Long id);

	// 강사가 출제한 시험 목록 조회
	@Query("SELECT e FROM Exam e " +
			"JOIN FETCH e.course c " +
			"JOIN c.instructor i " +
			"WHERE i.id = :instructorId")
	List<Exam> findByInstructorId(@Param("instructorId") Long instructorId);

	// 학생이 수강중인 강의의 시험 목록 조회
	@Query("SELECT DISTINCT e FROM Exam e " +
			"JOIN FETCH e.course c " +
			"JOIN c.courseStudents cs " +
			"JOIN cs.student s " +
			"WHERE s.id = :studentId")
	List<Exam> findByStudentId(@Param("studentId") Long studentId);

	// 특정 시험에 속한 문제 조회
	@Query("SELECT e FROM Exam e " +
			"LEFT JOIN FETCH e.questions q " +
			"LEFT JOIN FETCH q.image " +
			"WHERE e.id = :examId")
	Optional<Exam> findByIdWithQuestionsAndCourse(@Param("examId") Long examId);

	// 특정 상태의 시험 목록 조회
	List<Exam> findByStatusIn(Collection<ExamStatus> statuses);

}
