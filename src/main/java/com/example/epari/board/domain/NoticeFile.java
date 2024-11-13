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



//package com.example.epari.board.domain;
//
//import com.example.epari.global.common.base.BaseFile;
//import jakarta.persistence.*;
//import lombok.AccessLevel;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//
//
///**
// * 공지사항 첨부파일 관리 엔티티
// */
//
//@Entity
//@DiscriminatorValue("NOTICE_FILE")
//@PrimaryKeyJoinColumn(name = "notice_file_id")
//@Getter
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//public class NoticeFile extends BaseFile {
//
//	@ManyToOne(fetch = FetchType.LAZY)
//	@JoinColumn(name = "notice_id")
//	private Notice notice;
//
//	private NoticeFile(String originalFileName, String storedFileName, String fileUrl, Long fileSize) {
//		super(originalFileName, storedFileName, fileUrl, fileSize);
//	}
//
//	public static NoticeFile createNoticeFile(String originalFileName, String storedFileName,
//											  String fileUrl, Long fileSize, Notice notice) {
//		NoticeFile file = new NoticeFile(originalFileName, storedFileName, fileUrl, fileSize);
//		file.setNotice(notice);
//		return file;
//	}
//
//	public void setNotice(Notice notice) {
//		// 기존 관계 제거
//		if (this.notice != null) {
//			this.notice.getFiles().remove(this);
//		}
//		this.notice = notice;
//		// 새로운 관계 설정
//		if (notice != null && !notice.getFiles().contains(this)) {
//			notice.getFiles().add(this);
//		}
//	}
//
//	// s3
//	public void updateFileUrl(String fileUrl) {
//		this.fileUrl = fileUrl;
//	}
//
//}
