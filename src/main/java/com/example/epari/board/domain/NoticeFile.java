package com.example.epari.board.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "notice_files")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeFile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "notice_id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Notice notice;

	@Column(nullable = false)
	private String originalFileName;  // 원본 파일명

	@Column(nullable = false)
	private String storedFileName;    // S3에 저장된 파일명 (키)

	@Column(length = 2048)
	private String fileUrl;           // S3 URL

	private Long fileSize;            // 파일 크기

	@Builder
	private NoticeFile(String originalFileName, String storedFileName, String fileUrl, Long fileSize) {
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

	/**
	 * Notice 엔티티와 연관관계 설정
	 */
	public void setNotice(Notice notice) {
		// 기존 관계 제거
		if (this.notice != null) {
			this.notice.getFiles().remove(this);
		}
		this.notice = notice;
		// 양방향 관계 설정
		if (notice != null && !notice.getFiles().contains(this)) {
			notice.getFiles().add(this);
		}
	}

	/**
	 * FileUrl 업데이트
	 */
	public void updateFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}

	/**
	 * S3에 저장된 파일명(키) 반환
	 * storedFileName이 null일 경우 fileUrl에서 추출
	 */
	public String getStoredFileName() {
		if (this.storedFileName != null && !this.storedFileName.isEmpty()) {
			return this.storedFileName;
		}
		// fileUrl이 있을 경우 URL에서 키 추출 시도
		if (this.fileUrl != null && !this.fileUrl.isEmpty()) {
			int lastIndex = this.fileUrl.lastIndexOf('/');
			if (lastIndex >= 0 && lastIndex < this.fileUrl.length() - 1) {
				return this.fileUrl.substring(lastIndex + 1);
			}
		}
		throw new IllegalStateException("Neither storedFileName nor valid fileUrl exists");
	}

	/**
	 * 파일 정보 출력을 위한 toString 재정의
	 */
	@Override
	public String toString() {
		return "NoticeFile{" +
				"id=" + id +
				", originalFileName='" + originalFileName + '\'' +
				", storedFileName='" + storedFileName + '\'' +
				", fileSize=" + fileSize +
				'}';
	}
}
