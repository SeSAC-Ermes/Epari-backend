package com.example.epari.assignment.repository;

import com.example.epari.assignment.domain.AssignmentFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssignmentFileRepository extends JpaRepository<AssignmentFile, Long> {

}
