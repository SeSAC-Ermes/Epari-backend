package com.example.epari.assignment.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.epari.global.common.base.BaseTimeEntity;
import com.example.epari.lecture.domain.Lecture;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * 과제 관리를 위한 엔티티
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Assignment extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String title;           // 과제 제목

	@Column
	@Lob
	private String description;     // 과제 설명

	@Column
	private LocalDateTime deadline; // 마감기한

	@Column
	private Integer score;          // 배점

	private String feedback;        // 전체 피드백

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "lecture_id")
	private Lecture lecture;        // 강의

	@OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<AssignmentFile> files = new ArrayList<>();  // 과제 첨부파일

	@Builder
	private Assignment(String title, String description, LocalDateTime deadline,
			Integer score, String feedback, Lecture lecture) {
		this.title = title;
		this.description = description;
		this.deadline = deadline;
		this.score = score;
		this.feedback = feedback;
		this.lecture = lecture;
	}

	// 과제 생성
	public static Assignment createAssignment(String title, String description,
			LocalDateTime deadline, Integer score, Lecture lecture) {
		return Assignment.builder()
				.title(title)
				.description(description)
				.deadline(deadline)
				.score(score)
				.lecture(lecture)
				.build();
	}

	// 과제 정보 수정
	public void updateAssignment(String title, String description,
			LocalDateTime deadline, Integer score) {
		this.title = title;
		this.description = description;
		this.deadline = deadline;
		this.score = score;
	}

	// 파일 추가
	public void addFile(AssignmentFile file) {
		this.files.add(file);
	}

}
