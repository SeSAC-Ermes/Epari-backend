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
 * 답변 이미지 파일 정보를 관리하는 엔티티
 */
@Entity
@DiscriminatorValue("ANSWER_FILE")
@PrimaryKeyJoinColumn(name = "answer_file_id")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnswerFile extends BaseFile {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "answer_id")
	private Answer answer;

	public static AnswerFile createAnswerFile(String originalFileName, String storedFileName,
											  String fileUrl, Long fileSize, Answer answer) {
		AnswerFile file = new AnswerFile(originalFileName, storedFileName, fileUrl, fileSize);
		file.setAnswer(answer);
		return file;
	}

	private AnswerFile(String originalFileName, String storedFileName, String fileUrl, Long fileSize) {
		super(originalFileName, storedFileName, fileUrl, fileSize);
	}

	private void setAnswer(Answer answer) {
		this.answer = answer;
	}

}
