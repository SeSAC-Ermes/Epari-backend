package com.example.epari.lecture.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 강의 자료를 관리하는 엔티티
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class LectureContent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column
	private String content;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate date;  // 실제 강의 진행 날짜

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "lecture_id")
	private Lecture lecture;

	@OneToMany(mappedBy = "lectureContent", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<LectureContentFile> files = new ArrayList<>();

	@Builder
	private LectureContent(String title, String content, Lecture lecture, LocalDate date) {
		this.title = title;
		this.content = content;
		this.lecture = lecture;
		this.date = date;
	}

	// 파일 추가
	public void addFile(LectureContentFile file) {
		this.files.add(file);
	}

	// 파일 제거
	public void removeFile(LectureContentFile file) {
		this.files.remove(file);
	}

	public void updateContent(String title, String content) {
		this.title = title;
		this.content = content;
	}

}
