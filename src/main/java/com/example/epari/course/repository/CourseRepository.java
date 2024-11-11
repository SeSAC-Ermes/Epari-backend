package com.example.epari.course.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.epari.course.domain.Course;

/**
 * 강의 Repository 인터페이스
 * JPA를 사용해 강의 Entity의 데이터베이스 연산을 담당합니다.
 */
public interface CourseRepository extends JpaRepository<Course, Long> {

	/**
	 * 학생 id를 받아 강의 id 와 학생 id 일치로 조회 강의 조회
	 */
	@Query("SELECT l FROM Course l "
		   + "INNER JOIN CourseStudent ls ON l.id = ls.course.id "
		   + "WHERE ls.student.id = :studentId")
	List<Course> findAllByStudentId(@Param("studentId") Long studentId);

	/**
	 * 강사 id를 받아 강의 테이블에서 일치하는지 확인 강의 조회
	 */
	@Query("SELECT l FROM Course l "
		   + "WHERE l.instructor.id = :instructorId")
	List<Course> findAllByInstructorId(@Param("instructorId") Long instructorId);

	/**
	 * 주어진 강의 ID와 강사 이메일로 해당 강의가 해당 강사의 강의인지 확인합니다.
	 */
	@Query("""
			SELECT EXISTS (
			    SELECT 1
			    FROM Course l
			    WHERE l.id = :courseId
			    AND l.instructor.email = :instructorEmail
			)
			""")
	boolean existsByCourseIdAndInstructorEmail(Long courseId, String instructorEmail);

}