package com.example.epari.admin.dto;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 강의 생성 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCourseRequestDto {

	@NotBlank(message = "강의명은 필수입니다.")
	private String name;

	@NotNull(message = "시작일은 필수입니다.")
	private LocalDate startDate;

	@NotNull(message = "종료일은 필수입니다.")
	private LocalDate endDate;

	@NotBlank(message = "강의실은 필수입니다.")
	private String classroom;

	@NotNull(message = "강사 ID는 필수입니다.")
	private Long instructorId;

	private MultipartFile courseImage;  // 강의 이미지 (선택)

	// 커리큘럼 정보
	@NotNull(message = "커리큘럼은 필수입니다.")
	private List<CurriculumInfo> curriculums;

	// 날짜 유효성 검증
	public boolean isValidDateRange() {
		return !startDate.isAfter(endDate);
	}

	@Getter
	@Setter
	public static class CurriculumInfo {

		@NotNull(message = "강의 날짜는 필수입니다.")
		private LocalDate date;

		@NotBlank(message = "강의 주제는 필수입니다.")
		private String topic;

		private String description;  // 상세 설명 (선택)

	}

}
