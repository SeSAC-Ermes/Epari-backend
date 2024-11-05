package com.example.epari.lecture.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.epari.lecture.domain.LectureStudent;

public interface LectureStudentRepository extends JpaRepository<LectureStudent, Long> {

}
