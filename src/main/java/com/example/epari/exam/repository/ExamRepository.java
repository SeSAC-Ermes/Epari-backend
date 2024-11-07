package com.example.epari.exam.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.epari.exam.domain.Exam;

/**
 * 시험 정보에 대한 데이터베이스 접근을 담당하는 Repository 인터페이스
 * - Spring Data JPA를 통해 기본적인 CRUD 연산을 제공
 */
public interface ExamRepository extends JpaRepository<Exam, Long> {

}
