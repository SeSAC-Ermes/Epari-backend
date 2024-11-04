package com.example.epari.exam.domain;

import com.example.epari.global.common.base.BaseTimeEntity;
import com.example.epari.global.common.enums.ExamQuestionType;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 시험 문제의 공통 속성을 정의하는 추상 클래스
 * @Inheritance를 사용해 객관식/주관식 문제 타입을 구분하는 상속 구조
 */

@Entity
@Table(name = "exam_questions")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "question_type")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ExamQuestion extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String questionText;

	@Column(nullable = false)
	private int examNumber;

	@Column(nullable = false)
	private int score;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ExamQuestionType type;

	@Embedded
	private QuestionImage image;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "exam_id")
	private Exam exam;

	protected ExamQuestion(String questionText, int examNumber, int score,
			ExamQuestionType type, Exam exam) {
		this.questionText = questionText;
		this.examNumber = examNumber;
		this.score = score;
		this.type = type;
		this.exam = exam;
	}

	public void setImage(QuestionImage image) {
		this.image = image;
	}

	void setExam(Exam exam) {
		this.exam = exam;
	}

}
