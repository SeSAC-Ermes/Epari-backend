package com.example.epari.assignment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.epari.assignment.domain.Submission;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

}
