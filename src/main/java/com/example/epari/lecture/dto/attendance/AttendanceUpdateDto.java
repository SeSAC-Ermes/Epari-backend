package com.example.epari.lecture.dto.attendance;

import com.example.epari.global.common.enums.AttendanceStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceUpdateDto {

	@NotNull(message = "학생 ID는 필수입니다.")
	private Long studentId;

	@NotNull(message = "출석 상태는 필수입니다.")
	private AttendanceStatus status;

}
