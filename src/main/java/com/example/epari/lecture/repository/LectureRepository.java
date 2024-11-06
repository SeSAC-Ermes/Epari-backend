package com.example.epari.lecture.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.epari.lecture.domain.Lecture;

public interface LectureRepository extends JpaRepository<Lecture, Long> {

}
