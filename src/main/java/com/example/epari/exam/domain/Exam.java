package com.example.epari.exam.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.epari.course.domain.Course;
import com.example.epari.global.common.base.BaseTimeEntity;
import com.example.epari.global.common.enums.ExamStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * 시험의 생성, 수정, 삭제 및 문제 관리 기능을 제공
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
	private Exam(String title, LocalDateTime examDateTime, Integer duration, Integer totalScore, String description,
			Course course) {
		this.title = title;
		this.examDateTime = examDateTime;
		this.duration = duration;
		this.totalScore = totalScore;
		this.description = description;
		this.course = course;
	}

	//시험 종료 시간을 계산하여 반환
	public LocalDateTime getEndDateTime() {
		if (examDateTime == null || duration == null) {
			throw new IllegalStateException("시험 시작 시간 또는 시험 시간이 설정되지 않았습니다.");
		}
		if (duration <= 0) {
			throw new IllegalStateException("시험 시간은 0보다 커야 합니다.");
		}
		return examDateTime.plusMinutes(duration);
	}

	// 시험 정보 업데이트
	public void updateExam(String title, LocalDateTime examDateTime, Integer duration, Integer totalScore,
			String description) {
		this.title = title;
		this.examDateTime = examDateTime;
		this.duration = duration;
		this.totalScore = totalScore;
		this.description = description;
	}

	// 시험 문제 추가
	public void addQuestion(ExamQuestion question) {
		this.questions.add(question);
		question.setExam(this);
	}

	// 시험 문제 순서 재정렬
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

	// 시험 시작 전 여부 확인
	public boolean isBeforeExam() {
		if (examDateTime == null) {
			throw new IllegalStateException("시험 시작 시간이 설정되지 않았습니다.");
		}
		return LocalDateTime.now().isBefore(examDateTime);
	}

	// 시험 종료 여부 확인
	public boolean isAfterExam() {
		return LocalDateTime.now().isAfter(getEndDateTime());
	}

	// 시험 진행 중 여부 확인
	public boolean isDuringExam() {
		// LocalDateTime now = LocalDateTime.now();
		return !isBeforeExam() && !isAfterExam();
	}

	@Enumerated(EnumType.STRING)
	private ExamStatus status = ExamStatus.SCHEDULED;

	// 시험 상태 업데이트
	public void updateStatus(ExamStatus newStatus) {
		this.status = newStatus;
	}

}
