package com.example.epari.board.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class NoticeFile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "notice_id")
	private Notice notice;

	private String originalFileName;

	private String storedFileName;

	@Column(length = 2048)  // URL 길이를 2048로 증가
	private String fileUrl;  // 추가된 필드

	private Long fileSize;

	@Builder
	public NoticeFile(String originalFileName, String storedFileName, String fileUrl, Long fileSize) {
		this.originalFileName = originalFileName;
		this.storedFileName = storedFileName;
		this.fileUrl = fileUrl;
		this.fileSize = fileSize;
	}

	public static NoticeFile createNoticeFile(
			String originalFileName,
			String storedFileName,
			String fileUrl,
			Long fileSize,
			Notice notice
	) {
		NoticeFile noticeFile = NoticeFile.builder()
				.originalFileName(originalFileName)
				.storedFileName(storedFileName)
				.fileUrl(fileUrl)
				.fileSize(fileSize)
				.build();
		noticeFile.setNotice(notice);
		return noticeFile;
	}

	public void setNotice(Notice notice) {
		this.notice = notice;
	}

	public void updateFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}

}
