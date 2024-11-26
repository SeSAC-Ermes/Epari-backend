package com.example.epari.admin.dto;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 강의 수정 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCourseUpdateRequestDto {

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

	private MultipartFile courseImage;  // 새로운 강의 이미지 (선택)

	private boolean removeExistingImage;  // 기존 이미지 삭제 여부

	@Valid  // 중첩된 객체의 유효성 검사
	@NotNull(message = "커리큘럼은 필수입니다.")
	private List<CurriculumUpdateInfo> curriculums;

	// 날짜 유효성 검증
	public boolean isValidDateRange() {
		return !startDate.isAfter(endDate);
	}

	// 강의 기간 내의 모든 커리큘럼 날짜 유효성 검증
	public boolean isAllCurriculumDatesValid() {
		return curriculums.stream()
				.filter(c -> !c.deleted)  // 삭제 예정인 커리큘럼은 제외
				.allMatch(c -> c.isDateInRange(startDate, endDate));
	}

	@Getter
	@Setter
	public static class CurriculumUpdateInfo {

		private Long id;  // 기존 커리큘럼의 경우 ID 존재, 신규는 null

		@NotNull(message = "강의 날짜는 필수입니다.")
		private LocalDate date;

		@NotBlank(message = "강의 주제는 필수입니다.")
		private String topic;

		private String description;  // 상세 설명 (선택)

		private boolean deleted;  // 삭제 여부

		// 커리큘럼 날짜가 강의 기간 내에 있는지 검증
		public boolean isDateInRange(LocalDate courseStart, LocalDate courseEnd) {
			return !date.isBefore(courseStart) && !date.isAfter(courseEnd);
		}

	}

}
