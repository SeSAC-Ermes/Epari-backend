package com.example.epari.exam.domain;

import java.util.ArrayList;
import java.util.List;

import com.example.epari.global.common.enums.ExamQuestionType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ExamQuestion을 상속받아 객관식 문제를 구현
 */

@Entity
@DiscriminatorValue("MULTIPLE_CHOICE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class MultipleChoiceQuestion extends ExamQuestion {

	@OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("number asc")
	private List<Choice> choices = new ArrayList<>();

	@Builder
	private MultipleChoiceQuestion(String questionText, int examNumber, int score,
			Exam exam, String correctAnswer) {  // correctChoiceNumber 대신 correctAnswer 사용
		super(questionText, examNumber, score, ExamQuestionType.MULTIPLE_CHOICE, exam, correctAnswer);
	}

	@Override
	protected boolean doValidateAnswer(String studentAnswer) {
		try {
			// 선택지 번호가 숫자인지 확인
			int selectedNumber = Integer.parseInt(studentAnswer);

			// 유효한 선택지 범위인지 확인
			if (selectedNumber < 1 || selectedNumber > choices.size()) {
				return false;
			}

			// 정답과 일치하는지 확인
			return studentAnswer.equals(getCorrectAnswer());

		} catch (NumberFormatException e) {
			return false;
		}
	}

	public void addChoice(Choice choice) {
		this.choices.add(choice);
		choice.setQuestion(this);
	}

}
