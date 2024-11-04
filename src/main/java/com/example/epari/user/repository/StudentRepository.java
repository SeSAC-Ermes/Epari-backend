package com.example.epari.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.epari.user.domain.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {

}
