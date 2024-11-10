package com.example.epari.course.domain;

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
@DiscriminatorValue("COURSE_CONTENT_ATTACHMENT")
@PrimaryKeyJoinColumn(name = "course_content_attachment_id")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseContentFile extends BaseFile {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "course_content_id")
	private CourseContent courseContent;

	private CourseContentFile(String originalFileName, String storedFileName, String fileUrl, Long fileSize) {
		super(originalFileName, storedFileName, fileUrl, fileSize);
	}

	public static CourseContentFile createAttachment(String originalFileName, String storedFileName,
			String fileUrl, Long fileSize, CourseContent courseContent) {
		CourseContentFile attachment = new CourseContentFile(originalFileName, storedFileName, fileUrl,
				fileSize);
		attachment.setCourseContent(courseContent);
		return attachment;
	}

	private void setCourseContent(CourseContent courseContent) {
		this.courseContent = courseContent;
	}

}
