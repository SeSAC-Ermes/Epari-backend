package com.example.epari.assignment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.epari.assignment.domain.AssignmentFile;

@Repository
public interface AssignmentFileRepository extends JpaRepository<AssignmentFile, Long> {

	List<AssignmentFile> findFileByAssignmentId(Long assignmentId);

}
