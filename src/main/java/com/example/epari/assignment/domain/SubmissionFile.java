package com.example.epari.assignment.domain;

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
 * 과제 제출 파일 정보를 관리하는 엔티티
 */

@Entity
@DiscriminatorValue("SUBMISSION_FILE")
@PrimaryKeyJoinColumn(name = "submission_file_id")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubmissionFile extends BaseFile {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "submission_id")
	private Submission submission;

	public static SubmissionFile createSubmissionFile(String originalFileName, String storedFileName,
			String fileUrl, Long fileSize, Submission submission) {
		SubmissionFile file = new SubmissionFile(originalFileName, storedFileName, fileUrl, fileSize);
		file.setSubmission(submission);
		return file;
	}

	private SubmissionFile(String originalFileName, String storedFileName, String fileUrl, Long fileSize) {
		super(originalFileName, storedFileName, fileUrl, fileSize);
	}

	private void setSubmission(Submission submission) {
		this.submission = submission;
	}

}
