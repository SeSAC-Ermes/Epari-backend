package com.example.epari.exam.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.epari.exam.domain.ExamQuestion;

public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, Long> {

	// 시험별 문제 목록 조회
	List<ExamQuestion> findByExamId(Long examId);

	// 시험별 문제 번호순 정렬 조회
	List<ExamQuestion> findByExamIdOrderByExamNumberAsc(Long examId);

	// 시험에 속한 문제인지 확인
	boolean existsByIdAndExamId(Long questionId, Long examId);

}
