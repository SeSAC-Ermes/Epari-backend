package com.example.epari.assignment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.epari.assignment.domain.Assignment;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

	// instructor를 JOIN FETCH로 가져오는 메서드
	@Query("SELECT a FROM Assignment a LEFT JOIN FETCH a.instructor WHERE a.id = :assignmentId")
	Optional<Assignment> findByIdWithInstructor(@Param("assignmentId") Long assignmentId);

	// courseId로 조회할 때도 instructor 정보 함께 가져오는 메서드
	@Query("SELECT a FROM Assignment a LEFT JOIN FETCH a.instructor WHERE a.course.id = :courseId")
	List<Assignment> findByCourseIdWithInstructor(@Param("courseId") Long courseId);

	// 특정 강의 특정 과제 조회
	@Query("SELECT a from Assignment a WHERE a.id = :assignmentId AND a.course.id = :courseId")
	Optional<Assignment> findByIdAndCourseId(
			@Param("assignmentId") Long assignmentId,
			@Param("courseId") Long courseId
	);

	// 검색어를 포함하는 과제 찾는 메서드
	@Query("SELECT a FROM Assignment a LEFT JOIN FETCH a.instructor WHERE a.title LIKE %:title%")
	List<Assignment> findAssignmentByTitleContains(@Param("title") String title);

}
