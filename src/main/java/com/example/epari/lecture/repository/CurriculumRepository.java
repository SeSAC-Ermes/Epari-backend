package com.example.epari.lecture.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.epari.lecture.domain.Curriculum;

public interface CurriculumRepository extends JpaRepository<Curriculum, Long> {

}
