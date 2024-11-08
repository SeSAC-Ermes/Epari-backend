package com.example.epari.exam.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.epari.exam.domain.Exam;

/**
 * 시험 정보에 대한 데이터베이스 접근을 담당하는 Repository 인터페이스
 */
public interface ExamRepository extends JpaRepository<Exam, Long> {

	// 특정 강의에 속한 모든 시험 조회
	List<Exam> findByLectureId(Long lectureId);

}
