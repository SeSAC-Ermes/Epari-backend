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
	protected boolean doValidateAnswer(String studentAnswer) {
		// 대소문자 무시, 앞뒤 공백 제거 후 비교
		String normalizedCorrectAnswer = getCorrectAnswer().trim().toLowerCase();
		String normalizedStudentAnswer = studentAnswer.trim().toLowerCase();

		return normalizedCorrectAnswer.equals(normalizedStudentAnswer);
	}

}
