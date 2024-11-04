package com.example.epari.lecture.domain;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 커리큘럼을 표현하는 엔티티
 */
@Entity
@Table(name = "curriculums")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Curriculum {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private LocalDate date; // 강의 날짜

	@Column(nullable = false)
	private String topic; // 해당 일자 강의 주제

	private String description; // 강의 내용 설명

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "lecture_id")
	private Lecture lecture;

	@Builder
	private Curriculum(LocalDate date, String topic, String description, Lecture lecture) {
		this.date = date;
		this.topic = topic;
		this.description = description;
		this.lecture = lecture;
	}

}
