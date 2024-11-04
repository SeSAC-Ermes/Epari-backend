package com.example.epari.board.domain;

import com.example.epari.global.common.base.BaseTimeEntity;
import com.example.epari.global.common.enums.NoticeType;
import com.example.epari.lecture.domain.Lecture;
import com.example.epari.user.domain.Instructor;

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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 강의 공지사항을 관리하는 엔티티
 */
@Entity
@Table(name = "notices")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	@Lob
	private String content;

	@Column(nullable = false)
	private Integer viewCount = 0;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private NoticeType type;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "lecture_id", nullable = false)
	private Lecture lecture;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "instructor_id")
	private Instructor instructor;

	@Builder
	private Notice(String title, Instructor instructor,String content, NoticeType type, Lecture lecture) {
		this.title = title;
		this.instructor = instructor;
		this.content = content;
		this.type = type;
		this.lecture = lecture;
		this.viewCount = 0;
	}

	public void increaseViewCount() {
		this.viewCount++;
	}

	public void update(String title, String content) {
		this.title = title;
		this.content = content;
	}

}
