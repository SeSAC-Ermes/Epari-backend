package com.example.epari.assignment.domain;

import java.util.ArrayList;
import java.util.List;

import com.example.epari.global.common.base.BaseTimeEntity;
import com.example.epari.global.common.enums.SubmissionStatus;
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
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 과제 제출 정보를 관리하는 엔티티
 * - BaseEvaluation을 상속받아 과제 제출만의 추가 속성 관리
 */

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Submission extends BaseTimeEntity {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String title;

	@Column
	@Lob
	private String description;

	@Column
	private int score;

	private String feedback;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assignment_id")
	private Assignment assignment;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "student_id")
	private Student student;

	@OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<SubmissionFile> files = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SubmissionStatus status = SubmissionStatus.SUBMITTED;

	@Builder
	private Submission(String title, String description, Assignment assignment, Student student) {
		this.title = title;
		this.description = description;
		this.assignment = assignment;
		this.student = student;
		this.score = 0;
		this.status = SubmissionStatus.SUBMITTED;
	}


	public static Submission createSubmission(String title, String description,
			Assignment assignment, Student student) {
		return Submission.builder()
				.title(title)
				.description(description)
				.assignment(assignment)
				.student(student)
				.build();
	}


	public void updateScore(Integer score, String feedback) {
		this.score = score;
		this.feedback = feedback;
		this.status = SubmissionStatus.GRADED;
	}

	// 파일 추가
	public void addFile(SubmissionFile file) {
		this.files.add(file);
	}

}
