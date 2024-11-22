package com.example.epari.assignment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.epari.assignment.domain.Submission;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
	@Query("SELECT s FROM Submission s WHERE s.assignment.id = :assignmentId AND s.student.id = :studentId")
	Optional<Submission> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

	@Query("SELECT s FROM Submission s where s.assignment.id = :assignmentId")
	List<Submission> findByAssignmentId(Long assignmentId);

	@Query("SELECT s FROM Submission s " +
			"JOIN s.assignment a " +
			"JOIN a.course c " +
			"WHERE c.id = :courseId")
	List<Submission> findByCourseId(Long courseId);

	@Query("SELECT s FROM Submission s " +
			"LEFT JOIN FETCH s.student " +
			"JOIN FETCH s.assignment a " +
			"JOIN FETCH a.course c " +
			"WHERE a.id = :assignmentId " +
			"AND c.id = :courseId")
	List<Submission> findByAssignmentIdAndCourseIdWithStudent(Long assignmentId, Long courseId);
}
