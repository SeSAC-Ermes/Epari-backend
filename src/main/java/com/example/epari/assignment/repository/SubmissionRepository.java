package com.example.epari.assignment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.epari.assignment.domain.Submission;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
	@Query("SELECT s FROM Submission s WHERE s.assignment.id = :assignmentId AND s.student.id = :studentId")
	Optional<Submission> findByAssignment_IdAndStudent_Id(Long assignmentId, Long studentId);
}
