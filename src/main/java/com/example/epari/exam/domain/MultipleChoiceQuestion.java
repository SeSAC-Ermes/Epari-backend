package com.example.epari.exam.domain;

import java.util.ArrayList;
import java.util.List;

import com.example.epari.global.common.enums.ExamQuestionType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
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

	@Column(name = "correct_choice_number", nullable = false)
	private int correctChoiceNumber;

	@OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("number asc")
	private List<Choice> choices = new ArrayList<>();

	@Builder
	private MultipleChoiceQuestion(String questionText, int examNumber, int score,
			Exam exam, int correctChoiceNumber) {
		super(questionText, examNumber, score, ExamQuestionType.MULTIPLE_CHOICE, exam);
		this.correctChoiceNumber = correctChoiceNumber;
	}

	public void addChoice(Choice choice) {
		this.choices.add(choice);
		choice.setQuestion(this);
	}
}
