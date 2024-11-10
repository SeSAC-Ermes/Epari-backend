package com.example.epari.exam.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.epari.global.common.base.BaseTimeEntity;
import com.example.epari.course.domain.Course;

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
	private Exam(String title, LocalDateTime examDateTime, Integer duration,
			Integer totalScore, String description, Course course) {
		this.title = title;
		this.examDateTime = examDateTime;
		this.duration = duration;
		this.totalScore = totalScore;
		this.description = description;
		this.course = course;
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

}
