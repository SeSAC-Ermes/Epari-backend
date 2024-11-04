package com.example.epari.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.epari.user.domain.Instructor;

public interface InstructorRepository extends JpaRepository<Instructor, Long> {

}
