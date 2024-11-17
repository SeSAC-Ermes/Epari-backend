package com.example.epari.exam.domain;

import com.example.epari.global.common.enums.ExamQuestionType;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("SUBJECTIVE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SubjectiveQuestion extends ExamQuestion {

	@Builder
	private SubjectiveQuestion(String questionText, int examNumber, int score,
			Exam exam, String correctAnswer) {
		super(questionText, examNumber, score, ExamQuestionType.SUBJECTIVE, exam, correctAnswer);
	}

	@Override
	public boolean validateAnswer(String studentAnswer) {
		// 주관식은 좀 더 유연한 검증이 필요할 수 있음
		return getCorrectAnswer().trim().equalsIgnoreCase(studentAnswer.trim());
	}

}
