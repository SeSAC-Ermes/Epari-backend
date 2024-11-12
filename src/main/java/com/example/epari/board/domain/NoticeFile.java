package com.example.epari.board.domain;

import com.example.epari.global.common.base.BaseFile;
import jakarta.persistence.*;
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

	private NoticeFile(String originalFileName, String storedFileName, String fileUrl, Long fileSize) {
		super(originalFileName, storedFileName, fileUrl, fileSize);
	}

	public static NoticeFile createNoticeFile(String originalFileName, String storedFileName,
											  String fileUrl, Long fileSize, Notice notice) {
		NoticeFile file = new NoticeFile(originalFileName, storedFileName, fileUrl, fileSize);
		file.setNotice(notice);
		return file;
	}

	public void setNotice(Notice notice) {
		// 기존 관계 제거
		if (this.notice != null) {
			this.notice.getFiles().remove(this);
		}
		this.notice = notice;
		// 새로운 관계 설정
		if (notice != null && !notice.getFiles().contains(this)) {
			notice.getFiles().add(this);
		}
	}

}
