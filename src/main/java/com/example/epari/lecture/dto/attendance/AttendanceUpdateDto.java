package com.example.epari.lecture.dto.attendance;

import com.example.epari.global.common.enums.AttendanceStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 출석 상태 변경 요청 데이터를 담는 DTO 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceUpdateDto {

	@NotNull(message = "학생 ID는 필수입니다.")
	private Long studentId;

	@NotNull(message = "출석 상태는 필수입니다.")
	private AttendanceStatus status;

}
