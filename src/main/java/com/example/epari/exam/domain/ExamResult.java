package com.example.epari.exam.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.epari.global.common.base.BaseTimeEntity;
import com.example.epari.global.common.enums.ExamStatus;
import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;
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

	@Column(nullable = false)
	private Integer totalScore = 0;

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

	/**
	 * 답안 추가
	 */
	public void addScore(ExamScore score) {
		validateScoreAddable();
		scores.add(score);
		score.setExamResult(this);
	}

	/**
	 * 답안 제출 처리
	 * @param force 강제 제출 여부 (시험 시간 초과 등의 경우)
	 */
	public void submit(boolean force) {
		if (!force) {
			validateSubmittable();
		}
		this.submitTime = LocalDateTime.now();
		this.status = ExamStatus.SUBMITTED;
		scores.forEach(ExamScore::markAsSubmitted);
	}

	/**
	 * 임시저장 답안 여부 확인
	 */
	public boolean hasTemporaryAnswer(Long questionId) {
		return scores.stream()
				.anyMatch(score ->
						score.getQuestion().getId().equals(questionId) &&
								score.isTemporary()
				);
	}

	/**
	 * 채점 결과 반영
	 */
	public void updateScore() {
		if (status != ExamStatus.SUBMITTED) {
			throw new BusinessBaseException(ErrorCode.EXAM_NOT_SUBMITTED);
		}

		this.totalScore = calculateTotalScore();
		this.status = ExamStatus.GRADED;
	}

	public int getEarnedScore() {
		return scores.stream()
				.filter(score -> !score.isTemporary())
				.mapToInt(ExamScore::getEarnedScore)
				.sum();
	}

	/**
	 * 제출된 문제 수 조회
	 */
	public int getSubmittedQuestionCount() {
		return (int)scores.stream()
				.filter(score -> !score.isTemporary())
				.count();
	}

	/**
	 * 정답률 계산
	 */
	public double getCorrectAnswerRate() {
		if (scores.isEmpty()) {
			return 0.0;
		}

		long correctCount = scores.stream()
				.filter(score -> score.getEarnedScore() > 0)
				.count();

		return (double)correctCount / scores.size() * 100;
	}

	private int calculateTotalScore() {
		return scores.stream()
				.mapToInt(ExamScore::getEarnedScore)
				.sum();
	}

	private void validateExam(Exam exam) {
		if (exam == null) {
			throw new BusinessBaseException(ErrorCode.EXAM_NOT_FOUND);
		}
		if (exam.isAfterExam()) {
			throw new BusinessBaseException(ErrorCode.EXAM_ALREADY_ENDED);
		}
	}

	private void validateStudent(Student student) {
		if (student == null) {
			throw new BusinessBaseException(ErrorCode.STUDENT_NOT_FOUND);
		}
	}

	private void validateScoreAddable() {
		if (status == ExamStatus.SUBMITTED || status == ExamStatus.COMPLETED) {
			throw new BusinessBaseException(ErrorCode.EXAM_ALREADY_SUBMITTED);
		}
	}

	private void validateSubmittable() {
		if (status != ExamStatus.IN_PROGRESS) {
			throw new BusinessBaseException(ErrorCode.EXAM_NOT_IN_PROGRESS);
		}
		validateAllQuestionsAnswered();
	}

	private void validateAllQuestionsAnswered() {
		int totalQuestions = exam.getQuestions().size();
		long answeredQuestions = scores.stream()
				.filter(score -> !score.isTemporary())
				.count();
		if (answeredQuestions < totalQuestions) {
			throw new BusinessBaseException(ErrorCode.EXAM_NOT_ALL_QUESTIONS_ANSWERED);
		}
	}

}
