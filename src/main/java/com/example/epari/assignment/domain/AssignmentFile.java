package com.example.epari.assignment.domain;

import com.example.epari.global.common.base.BaseFile;
import jakarta.persistence.*;
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

	private AssignmentFile(String originalFileName, String storedFileName, String fileUrl, Long fileSize) {
		super(originalFileName, storedFileName, fileUrl, fileSize);
	}

	public static AssignmentFile createAssignmentFile(String originalFileName, String storedFileName, String fileUrl,
													  Long fileSize, Assignment assignment) {
		AssignmentFile assignmentFile = new AssignmentFile(originalFileName, storedFileName, fileUrl, fileSize);
		assignmentFile.setAssignment(assignment);
		return assignmentFile;
	}

	private void setAssignment(Assignment assignment) {
		this.assignment = assignment;
	}

}
