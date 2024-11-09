package com.example.epari.course.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.epari.course.domain.Curriculum;

public interface CurriculumRepository extends JpaRepository<Curriculum, Long> {

}
