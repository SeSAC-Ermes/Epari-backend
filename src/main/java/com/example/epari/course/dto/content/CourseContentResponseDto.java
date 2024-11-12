package com.example.epari.course.dto.content;

import java.time.LocalDate;
import java.util.List;

import com.example.epari.course.domain.CourseContent;
import com.example.epari.course.domain.CourseContentFile;
import com.example.epari.user.domain.Instructor;

import lombok.Builder;
import lombok.Getter;

/**
 * 강의 컨텐츠 조회 결과를 담는 응답 DTO
 */
@Getter
@Builder
public class CourseContentResponseDto {

	private Long id;

	private String title;

	private String content;

	private LocalDate date;

	private List<FileInfo> files;

	private InstructorInfo instructor;

	@Getter
	@Builder
	public static class FileInfo {

		private Long id;

		private String originalFileName;

		private String fileUrl;

		private Long fileSize;

		public static FileInfo from(CourseContentFile file) {
			return FileInfo.builder()
					.id(file.getId())
					.originalFileName(file.getOriginalFileName())
					.fileUrl(file.getFileUrl())
					.fileSize(file.getFileSize())
					.build();
		}

	}

	@Getter
	@Builder
	public static class InstructorInfo {

		private Long id;

		private String name;

		private String email;

		public static InstructorInfo from(Instructor instructor) {
			return InstructorInfo.builder()
					.id(instructor.getId())
					.name(instructor.getName())
					.email(instructor.getEmail())
					.build();
		}

	}

	public static CourseContentResponseDto from(CourseContent content) {
		return CourseContentResponseDto.builder()
				.id(content.getId())
				.title(content.getTitle())
				.content(content.getContent())
				.date(content.getDate())
				.files(content.getFiles().stream()
						.map(FileInfo::from)
						.toList())
				.instructor(InstructorInfo.from(content.getCourse().getInstructor()))
				.build();
	}

}
