package com.example.epari.admin.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 수강생 목록 업데이트 요청을 담는 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CourseStudentUpdateRequestDTO {

	@NotEmpty(message = "수강생 목록은 비어있을 수 없습니다.")
	private List<Long> studentIds;

}
