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

	private SubmissionFile(String originalFileName, String storedFileName, String fileUrl, Long fileSize,
			Submission submission) {
		super(originalFileName, storedFileName, fileUrl, fileSize);
		this.submission = submission;
		submission.getFiles().add(this);
	}

	public static SubmissionFile createSubmissionFile(String originalFileName, String storedFileName,
			String fileUrl, Long fileSize, Submission submission) {

		return new SubmissionFile(originalFileName, storedFileName, fileUrl, fileSize, submission);
	}

}
