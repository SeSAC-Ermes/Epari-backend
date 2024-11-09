package com.example.epari.course.dto.attendance;

import com.example.epari.course.domain.Attendance;

import lombok.Builder;
import lombok.Getter;

/**
 * 학생의 출석 정보를 응답하기 위한 DTO 클래스
 */
@Getter
public class AttendanceResponseDto {

	private final Long studentId;

	private final String name;

	private final String status;

	@Builder
	private AttendanceResponseDto(Long studentId, String name, String status) {
		this.studentId = studentId;
		this.name = name;
		this.status = status;
	}

	public static AttendanceResponseDto from(Attendance attendance) {
		return AttendanceResponseDto.builder()
				.studentId(attendance.getCourseStudent().getStudent().getId())
				.name(attendance.getCourseStudent().getStudent().getName())
				.status(attendance.getStatus().getDescription())
				.build();
	}

}
