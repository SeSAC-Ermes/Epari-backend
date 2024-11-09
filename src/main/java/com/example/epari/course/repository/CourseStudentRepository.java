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
			JOIN FETCH ls.student
			WHERE ls.course.id = :courseId
			""")
	List<CourseStudent> findAllCourseStudentsByCourseId(
			@Param("courseId") Long courseId
	);

}
