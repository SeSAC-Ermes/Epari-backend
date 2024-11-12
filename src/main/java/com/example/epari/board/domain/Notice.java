package com.example.epari.board.domain;

import com.example.epari.course.domain.Course;
import com.example.epari.global.common.base.BaseTimeEntity;
import com.example.epari.global.common.enums.NoticeType;
import com.example.epari.user.domain.Instructor;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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

	// files 필드 추가
	@OneToMany(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<NoticeFile> files = new ArrayList<>();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "course_id", nullable = false)
	private Course course;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "instructor_id")
	private Instructor instructor;

	@Builder
	private Notice(String title, Instructor instructor, String content, NoticeType type, Course course) {
		this.title = title;
		this.instructor = instructor;
		this.content = content;
		this.type = type;
		this.course = course;
		this.viewCount = 0;
	}

	public void increaseViewCount() {
		this.viewCount++;
	}

	public void update(String title, String content) {
		this.title = title;
		this.content = content;
	}

	// 파일 추가 메서드
	public void addFile(NoticeFile file) {
		this.files.add(file);
		if (file.getNotice() != this) {
			file.setNotice(this);
		}
	}

	
	// 공지사항 업데이트
	public void update(String title, String content, NoticeType type, Course course) {
		this.title = title;
		this.content = content;
		this.type = type;
		this.course = course;
	}

}
