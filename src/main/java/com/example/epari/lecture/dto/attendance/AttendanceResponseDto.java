package com.example.epari.lecture.dto.attendance;

import com.example.epari.global.common.enums.AttendanceStatus;
import com.example.epari.lecture.domain.Attendance;

import lombok.Builder;
import lombok.Getter;

/**
 * 학생의 출석 정보를 응답하기 위한 DTO 클래스
 */
@Getter
public class AttendanceResponseDto {

	private final Long studentId;

	private final String name;

	private final AttendanceStatus status;

	@Builder
	private AttendanceResponseDto(Long studentId, String name, AttendanceStatus status) {
		this.studentId = studentId;
		this.name = name;
		this.status = status;
	}

	public static AttendanceResponseDto from(Attendance attendance) {
		return AttendanceResponseDto.builder()
				.studentId(attendance.getLectureStudent().getStudent().getId())
				.name(attendance.getLectureStudent().getStudent().getName())
				.status(attendance.getStatus())
				.build();
	}

}
