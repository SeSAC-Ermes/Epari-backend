package com.example.epari.course.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.epari.course.domain.CourseStudent;

public interface CourseStudentRepository extends JpaRepository<CourseStudent, Long> {

	/**
	 * 특정 강의의 모든 수강생 조회
	 */
	@Query("""
			SELECT ls
			FROM CourseStudent ls
			JOIN FETCH ls.student s
			LEFT JOIN FETCH s.profileImage
			WHERE ls.course.id = :courseId
			""")
	List<CourseStudent> findAllCourseStudentsByCourseId(
			@Param("courseId") Long courseId
	);

	/**
	 * 특정 강의의 수강생 수 조회
	 */
	@Query("SELECT COUNT(cs) FROM CourseStudent cs WHERE cs.course.id = :courseId")
	int countByCourseId(@Param("courseId") Long courseId);

	/**
	 * 특정 강의의 특정 학생 수강 여부 확인
	 */
	@Query("""
			SELECT COUNT(cs) > 0 
			FROM CourseStudent cs 
			WHERE cs.course.id = :courseId 
			AND cs.student.id = :studentId
			""")
	boolean existsByCourseIdAndStudentId(
			@Param("courseId") Long courseId,
			@Param("studentId") Long studentId
	);

}
