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
 * 공지사항 첨부파일 관리 엔티티
 */
@Entity
@DiscriminatorValue("NOTICE_FILE")
@PrimaryKeyJoinColumn(name = "notice_file_id")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeFile extends BaseFile {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "notice_id")
	private Notice notice;

	public static NoticeFile createNoticeFile(String originalFileName, String storedFileName,
			String fileUrl, Long fileSize, Notice notice) {
		NoticeFile file = new NoticeFile(originalFileName, storedFileName, fileUrl, fileSize);
		file.setNotice(notice);
		return file;
	}

	private NoticeFile(String originalFileName, String storedFileName, String fileUrl, Long fileSize) {
		super(originalFileName, storedFileName, fileUrl, fileSize);
	}

	private void setNotice(Notice notice) {
		this.notice = notice;
	}

}
