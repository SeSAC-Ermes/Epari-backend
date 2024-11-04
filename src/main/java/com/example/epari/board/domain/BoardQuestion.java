package com.example.epari.board.domain;

import java.util.ArrayList;
import java.util.List;

import com.example.epari.global.common.base.BaseTimeEntity;
import com.example.epari.global.common.enums.BoardQuestionType;
import com.example.epari.lecture.domain.Lecture;
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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * QnA 질문 엔티티
 */

@Entity
@Table(name = "board_questions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardQuestion extends BaseTimeEntity {

	//나중에 익명 기능?

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String title;

	@Column
	@Lob
	private String content;

	@Column
	private int viewCount = 0;

	@Enumerated(EnumType.STRING)
	@Column
	private BoardQuestionType visibility = BoardQuestionType.PUBLIC; // 기본값 공개

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "lecture_id")
	private Lecture lecture;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "student_id")
	private Student student;

	/**
	 * cascade = CascadeType.ALL: 부모 엔티티(BoardQuestion)의 변경이 자식 엔티티(Attachment)에 모두 전파됨
	 * -저장, 삭제, 수정 등 모든 작업이 자동으로 전파
	 * orphanRemoval = true: 부모 엔티티와 연관관계가 끊어진 자식 엔티티를 자동으로 삭제
	 * -첨부파일이 게시글에서 제거되면 실제 데이터도 삭제됨
	 */
	@OneToMany(mappedBy = "boardQuestion", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<QuestionFile> questionFiles = new ArrayList<>();

	@Builder
	private BoardQuestion(String title, String content, BoardQuestionType visibility,
			Lecture lecture, Student student) {
		this.title = title;
		this.content = content;
		this.visibility = visibility;
		this.lecture = lecture;
		this.student = student;
		this.viewCount = 0;
	}

	public static BoardQuestion createBoardQuestion(String title, String content,
			BoardQuestionType visibility, Lecture lecture, Student student) {
		return BoardQuestion.builder()
				.title(title)
				.content(content)
				.visibility(visibility)
				.lecture(lecture)
				.student(student)
				.build();
	}

	// 파일 추가
	public void addAttachment(QuestionFile questionFile) {
		this.questionFiles.add(questionFile);
	}

	// 파일 제거
	public void removeAttachment(QuestionFile questionFile) {
		this.questionFiles.remove(questionFile);
	}

}
