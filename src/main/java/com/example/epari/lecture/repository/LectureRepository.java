package com.example.epari.lecture.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.epari.lecture.domain.Lecture;

/**
 * 강의 Repository 인터페이스
 * JPA를 사용해 강의 Entity의 데이터베이스 연산을 담당합니다.
 */
public interface LectureRepository extends JpaRepository<Lecture, Long> {

}
