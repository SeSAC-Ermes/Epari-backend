package com.example.epari.exam.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.epari.global.common.base.BaseTimeEntity;
import com.example.epari.global.common.enums.ExamStatus;
import com.example.epari.user.domain.Student;

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
 * 학생의 시험 제출 결과를 관리하는 엔티티
 */
@Entity
@Table(name = "exam_results")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExamResult extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "exam_id")
	private Exam exam;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "student_id")
	private Student student;

	@Column(nullable = false)
	private LocalDateTime submitTime;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ExamStatus status;

	@OneToMany(mappedBy = "examResult", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ExamScore> scores = new ArrayList<>();

	@Builder
	private ExamResult(Exam exam, Student student, LocalDateTime submitTime, ExamStatus status) {
		validateExam(exam);
		validateStudent(student);
		this.exam = exam;
		this.student = student;
		this.submitTime = submitTime != null ? submitTime : LocalDateTime.now();
		this.status = status != null ? status : ExamStatus.IN_PROGRESS;
		this.scores = new ArrayList<>();
	}

	public void updateStatus(ExamStatus status) {
		this.status = status;
	}

	public void addScore(ExamScore score) {
		validateScoreAddable();
		scores.add(score);
		score.setExamResult(this);
	}

	public void submit(boolean force) {
		if (!force) {
			validateSubmittable();
		}
		this.submitTime = LocalDateTime.now();
		this.status = ExamStatus.SUBMITTED;
		scores.forEach(ExamScore::markAsSubmitted);
	}

	public int getEarnedScore() {
		return scores.stream()
				.filter(score -> !score.isTemporary())
				.mapToInt(ExamScore::getEarnedScore)
				.sum();
	}

	private void validateExam(Exam exam) {
		if (exam == null) {
			throw new IllegalArgumentException("시험 정보는 필수입니다.");
		}
		if (exam.isAfterExam()) {
			throw new IllegalStateException("종료된 시험입니다.");
		}
	}

	private void validateStudent(Student student) {
		if (student == null) {
			throw new IllegalArgumentException("학생 정보는 필수입니다.");
		}
	}

	private void validateScoreAddable() {
		if (status == ExamStatus.SUBMITTED || status == ExamStatus.COMPLETED) {
			throw new IllegalStateException("이미 제출된 시험에는 답안을 추가할 수 없습니다.");
		}
	}

	private void validateSubmittable() {
		if (status != ExamStatus.IN_PROGRESS) {
			throw new IllegalStateException("진행 중인 시험만 제출할 수 있습니다.");
		}
		validateAllQuestionsAnswered();
	}

	private void validateAllQuestionsAnswered() {
		int totalQuestions = exam.getQuestions().size();
		long answeredQuestions = scores.stream()
				.filter(score -> !score.isTemporary())
				.count();
		if (answeredQuestions < totalQuestions) {
			throw new IllegalStateException("모든 문제에 답해야 제출할 수 있습니다.");
		}
	}

	public int getSubmittedQuestionCount() {
		return (int)scores.stream()
				.filter(score -> !score.isTemporary())
				.count();
	}

	public boolean hasTemporaryAnswer(Long questionId) {
		return scores.stream()
				.anyMatch(score ->
						score.getQuestion().getId().equals(questionId) &&
								score.isTemporary()
				);
	}

}
