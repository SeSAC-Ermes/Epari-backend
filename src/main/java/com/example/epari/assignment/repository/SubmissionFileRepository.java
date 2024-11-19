package com.example.epari.assignment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.epari.assignment.domain.SubmissionFile;

@Repository
public interface SubmissionFileRepository extends JpaRepository<SubmissionFile, Long> {

}
