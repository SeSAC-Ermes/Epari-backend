package com.example.epari.exam.domain;

import com.example.epari.global.common.base.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 개별 문제에 대한 학생의 답안과 채점 결과를 관리하는 엔티티
 */
@Entity
@Table(name = "exam_scores")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExamScore extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "exam_result_id")
	private ExamResult examResult;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "question_id")
	private ExamQuestion question;

	@Column(nullable = false)
	private String studentAnswer;

	@Column(nullable = false)
	private int earnedScore;

	@Column(nullable = false)
	private boolean temporary;

	@Builder
	public ExamScore(ExamResult examResult, ExamQuestion question, String studentAnswer, boolean temporary) {
		this.examResult = examResult;
		this.question = question;
		this.studentAnswer = studentAnswer;
		this.temporary = temporary;
		this.earnedScore = 0;
	}

	void setExamResult(ExamResult examResult) {
		this.examResult = examResult;
	}

	// 답안 업데이트
	public void updateAnswer(String answer) {
		this.studentAnswer = answer;
	}

	// 임시저장 상태 해제 (최종 제출)
	public void markAsSubmitted() {
		this.temporary = false;
	}

	// 채점 결과 업데이트
	public void updateScore(int score) {
		this.earnedScore = score;
	}

}
