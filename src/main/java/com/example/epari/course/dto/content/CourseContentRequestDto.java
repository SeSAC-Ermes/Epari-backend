package com.example.epari.course.dto.content;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;

/**
 * 강의 컨텐츠 관련 요청 데이터를 담는 DTO 클래스들
 */
public class CourseContentRequestDto {

	@Getter
	@Setter
	public static class Upload {

		private String title;

		private String content;

		private LocalDate date;

		private List<MultipartFile> files;

	}

	@Getter
	@Setter
	public static class Update {

		private String title;

		private String content;

		private List<MultipartFile> files;

	}

}
