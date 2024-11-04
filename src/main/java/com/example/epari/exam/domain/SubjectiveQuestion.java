package com.example.epari.exam.domain;

import com.example.epari.global.common.enums.ExamQuestionType;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ExamQuestion을 상속받아 주관식 문제를 구현
 */
@Entity
@DiscriminatorValue("SUBJECTIVE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SubjectiveQuestion extends ExamQuestion {

	@Column(name = "correct_answer", nullable = false)
	private String correctAnswer;

	@Builder
	private SubjectiveQuestion(String questionText, int examNumber, int score,
			Exam exam, String correctAnswer) {
		super(questionText, examNumber, score, ExamQuestionType.SUBJECTIVE, exam);
		this.correctAnswer = correctAnswer;
	}

	public void updateCorrectAnswer(String correctAnswer) {
		this.correctAnswer = correctAnswer;
	}

}
