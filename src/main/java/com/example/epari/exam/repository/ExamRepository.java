package com.example.epari.exam.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.epari.exam.domain.Exam;

/**
 * 시험 정보에 대한 데이터베이스 접근을 담당하는 Repository 인터페이스
 */
public interface ExamRepository extends JpaRepository<Exam, Long> {

	// 특정 강의에 속한 모든 시험 조회
	List<Exam> findByCourseId(Long courseId);

	// 특정 강의에 속한 시험 상세 조회
	Optional<Exam> findByCourseIdAndId(Long courseId, Long Id);

}
