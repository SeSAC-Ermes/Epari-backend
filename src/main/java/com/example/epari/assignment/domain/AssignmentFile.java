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
 * 과제 첨부파일 관리 엔티티
 */
@Entity
@DiscriminatorValue("ASSIGNMENT_FILE")
@PrimaryKeyJoinColumn(name = "assignment_file_id")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AssignmentFile extends BaseFile {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assignment_id")
	private Assignment assignment;

	private AssignmentFile(String originalFileName, String storedFileName, String fileUrl, Long fileSize,
			Assignment assignment) {
		super(originalFileName, storedFileName, fileUrl, fileSize);
		this.assignment = assignment;
		assignment.getFiles().add(this);
	}

	public static AssignmentFile createAssignmentFile(String originalFileName, String storedFileName, String fileUrl,
			Long fileSize, Assignment assignment) {
		
		return new AssignmentFile(originalFileName, storedFileName, fileUrl, fileSize, assignment);
	}

}
