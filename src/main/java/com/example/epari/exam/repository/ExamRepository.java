package com.example.epari.exam.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.epari.exam.domain.Exam;

/**
 * 시험 정보에 대한 데이터베이스 접근을 담당하는 Repository 인터페이스
 */
public interface ExamRepository extends JpaRepository<Exam, Long> {

	// 특정 강의에 속한 모든 시험 조회
	List<Exam> findByCourseId(Long courseId);

	// 특정 강의의 시험 목록 조회
	@Query("SELECT e FROM Exam e " +
			"WHERE e.course.id = :courseId " +
			"ORDER BY e.examDateTime DESC")
	List<Exam> findByCourseIdOrderByExamDateTimeDesc(@Param("courseId") Long courseId);

	// 특정 강의에 속한 시험 상세 조회
	Optional<Exam> findByCourseIdAndId(Long courseId, Long id);

	// 강사가 출제한 시험 목록 조회
	@Query("SELECT e FROM Exam e " +
			"JOIN FETCH e.course c " +
			"JOIN c.instructor i " +
			"WHERE i.email = :instructorEmail")
	List<Exam> findByInstructorEmail(@Param("instructorEmail") String instructorEmail);

	// 강사가 담당하는 강의의 모든 시험 조회
	@Query("SELECT e FROM Exam e "
			+ "JOIN FETCH e.course c "
			+ "JOIN c.instructor i "
			+ "WHERE i.email = :instructorEmail")
	List<Exam> findByInstructorEmail(String instructorEmail);

	// 학생이 수강중인 강의의 시험 목록 조회
	@Query("SELECT DISTINCT e FROM Exam e " +
			"JOIN FETCH e.course c " +
			"JOIN c.courseStudents cs " +
			"JOIN cs.student s " +
			"WHERE s.email = :studentEmail")
	List<Exam> findByStudentEmail(@Param("studentEmail") String studentEmail);

	@Query("SELECT e FROM Exam e " +
			"LEFT JOIN FETCH e.questions q " +
			"LEFT JOIN FETCH q.image " +
			"WHERE e.id = :examId")
	Optional<Exam> findByIdWithQuestionsAndCourse(@Param("examId") Long examId);

	// 학생이 수강중인 강의의 모든 시험 조회
	@Query("SELECT e FROM Exam e "
			+ "JOIN FETCH e.course c "
			+ "JOIN CourseStudent cs ON cs.course = c "
			+ "WHERE cs.student.email = :studentEmail")
	List<Exam> findByStudentEmail(String studentEmail);

}
