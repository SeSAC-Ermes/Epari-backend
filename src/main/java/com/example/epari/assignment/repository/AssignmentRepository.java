package com.example.epari.assignment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.epari.assignment.domain.Assignment;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

	List<Assignment> findAssignmentByTitleContains(String title);

}
