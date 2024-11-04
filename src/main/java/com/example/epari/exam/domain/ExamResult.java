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

	@OneToMany(mappedBy = "examResult", cascade = CascadeType.ALL)
	private List<ExamScore> scores = new ArrayList<>();

	@Builder
	private ExamResult(Exam exam, Student student) {
		this.exam = exam;
		this.student = student;
		this.submitTime = LocalDateTime.now();
		this.status = ExamStatus.SUBMITTED;
	}

	public void updateStatus(ExamStatus status) {
		this.status = status;
	}

	public void addScore(ExamScore score) {
		this.scores.add(score);
		score.setExamResult(this);
	}
}
