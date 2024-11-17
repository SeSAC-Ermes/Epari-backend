package com.example.epari.exam.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.epari.course.domain.Course;
import com.example.epari.global.common.base.BaseTimeEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 시험의 기본 정보를 관리하는 메인 엔티티
 */
@Entity
@Table(name = "exams")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Exam extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private LocalDateTime examDateTime;

	@Column(nullable = false)
	private Integer duration;

	@Column(nullable = false)
	private Integer totalScore;

	@Column(length = 1000)
	private String description;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "course_id")
	private Course course;

	@OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ExamQuestion> questions = new ArrayList<>();

	@Builder
	public static Exam createExam(
			String title,
			LocalDateTime examDateTime,
			Integer duration,
			Integer totalScore,
			String description,
			Course course) {
		Exam exam = new Exam();
		exam.title = title;
		exam.examDateTime = examDateTime;
		exam.duration = duration;
		exam.totalScore = totalScore;
		exam.description = description;
		exam.course = course;
		exam.questions = new ArrayList<>();
		return exam;
	}

	public void updateExam(String title, LocalDateTime examDateTime,
			Integer duration, Integer totalScore, String description) {
		this.title = title;
		this.examDateTime = examDateTime;
		this.duration = duration;
		this.totalScore = totalScore;
		this.description = description;
	}

	public void addQuestion(ExamQuestion question) {
		this.questions.add(question);
		question.setExam(this);
	}

	public void reorderQuestions(Long questionId, int newNumber) {
		// 1. 이동할 문제 찾기
		ExamQuestion targetQuestion = questions.stream()
				.filter(q -> q.getId().equals(questionId))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("문제를 찾을 수 없습니다. ID: " + questionId));

		// 2. 현재 문제 번호
		int currentNumber = targetQuestion.getExamNumber();

		// 3. 유효성 검사
		if (newNumber < 1 || newNumber > questions.size()) {
			throw new IllegalArgumentException("유효하지 않은 문제 번호입니다: " + newNumber);
		}

		// 4. 문제 번호 재정렬
		if (currentNumber < newNumber) {
			// 현재 위치에서 뒤로 이동하는 경우
			questions.stream()
					.filter(q -> q.getExamNumber() > currentNumber && q.getExamNumber() <= newNumber)
					.forEach(q -> q.updateExamNumber(q.getExamNumber() - 1));
		} else if (currentNumber > newNumber) {
			// 현재 위치에서 앞으로 이동하는 경우
			questions.stream()
					.filter(q -> q.getExamNumber() >= newNumber && q.getExamNumber() < currentNumber)
					.forEach(q -> q.updateExamNumber(q.getExamNumber() + 1));
		}

		// 5. 대상 문제의 번호 업데이트
		targetQuestion.updateExamNumber(newNumber);
	}

	public void reorderQuestionsAfterDelete(Long deletedQuestionId) {
		ExamQuestion deletedQuestion = questions.stream()
				.filter(q -> q.getId().equals(deletedQuestionId))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("삭제된 문제를 찾을 수 없습니다."));

		int deletedNumber = deletedQuestion.getExamNumber();

		questions.stream()
				.filter(q -> q.getExamNumber() > deletedNumber)
				.forEach(q -> q.updateExamNumber(q.getExamNumber() - 1));
	}

}
