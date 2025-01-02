package com.example.epari.exam.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.epari.exam.domain.ExamQuestion;

/**
 * 시험 문제 리포지토리
 */
public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, Long> {

	// 시험별 문제 번호순 정렬 조회
	List<ExamQuestion> findByExamIdOrderByExamNumberAsc(Long examId);

	// 시험에 속한 문제인지 확인
	@Query("SELECT q FROM ExamQuestion q WHERE q.exam.id = :examId AND q.id = :questionId")
	Optional<ExamQuestion> findByExamIdAndId(
			@Param("examId") Long examId,
			@Param("questionId") Long questionId
	);

	// 시험 문제에 대한 응시 제출 여부 확인
	@Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM ExamScore s WHERE s.question.id = :questionId")
	boolean hasSubmissions(@Param("questionId") Long questionId);

}
