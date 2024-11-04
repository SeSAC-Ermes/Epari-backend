package com.example.epari.lecture.domain;

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
 * 강의 자료 첨부파일을 관리하는 엔티티
 */
@Entity
@DiscriminatorValue("LECTURE_CONTENT_ATTACHMENT")
@PrimaryKeyJoinColumn(name = "lecture_content_attachment_id")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LectureContentFile extends BaseFile {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "lecture_content_id")
	private LectureContent lectureContent;

	private LectureContentFile(String originalFileName, String storedFileName, String fileUrl, Long fileSize) {
		super(originalFileName, storedFileName, fileUrl, fileSize);
	}

	public static LectureContentFile createAttachment(String originalFileName, String storedFileName,
			String fileUrl, Long fileSize, LectureContent lectureContent) {
		LectureContentFile attachment = new LectureContentFile(originalFileName, storedFileName, fileUrl,
				fileSize);
		attachment.setLectureContent(lectureContent);
		return attachment;
	}

	private void setLectureContent(LectureContent lectureContent) {
		this.lectureContent = lectureContent;
	}

}
