package com.example.epari.board.domain;

import com.example.epari.global.common.base.BaseFile;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * QnA 게시글 첨부파일 정보 관리 엔티티
 */
@Entity
@DiscriminatorValue("QUESTION_FILE")
@PrimaryKeyJoinColumn(name = "question_file_id")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionFile extends BaseFile {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "board_question_id")
	private BoardQuestion boardQuestion;

	public static QuestionFile createQuestionFile(String originalFileName, String storedFileName,
												  String fileUrl, Long fileSize, BoardQuestion boardQuestion) {
		QuestionFile file = new QuestionFile(originalFileName, storedFileName, fileUrl, fileSize);
		file.setBoardQuestion(boardQuestion);
		return file;
	}

	private QuestionFile(String originalFileName, String storedFileName, String fileUrl, Long fileSize) {
		super(originalFileName, storedFileName, fileUrl, fileSize);
	}

	private void setBoardQuestion(BoardQuestion boardQuestion) {
		this.boardQuestion = boardQuestion;
	}

}
