package com.example.epari.exam.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.epari.exam.domain.Exam;

/**
 * 시험 정보에 대한 데이터베이스 접근을 담당하는 Repository 인터페이스
 */
public interface ExamRepository extends JpaRepository<Exam, Long> {

	/**
	 * 1. 강의
	 */
	// 특정 강의에 속한 모든 시험 조회
	List<Exam> findByCourseId(Long courseId);

	// 특정 강의에 속한 시험 상세 조회
	Optional<Exam> findByCourseIdAndId(Long courseId, Long Id);

	/**
	 * 2. 강사
	 */
	// 강사가 담당하는 강의의 모든 시험 조회
	@Query("SELECT e FROM Exam e JOIN e.course c WHERE c.instructor.id = :instructorId")
	List<Exam> findByInstructorId(Long instructorId);

	/**
	 * 3. 학생
	 */
	// 학생이 수강중인 강의의 모든 시험 조회
	@Query("SELECT e FROM Exam e JOIN e.course c JOIN CourseStudent cs WHERE cs.student.id = :student.Id AND cs.course = c")
	List<Exam> findByStudentId(Long studentId);

}
