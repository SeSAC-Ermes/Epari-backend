package com.example.epari.course.dto.attendance;

import com.example.epari.user.domain.Student;

import lombok.Builder;
import lombok.Getter;

/**
 * 학생별 출석 통계 정보를 전달하기 위한 DTO 클래스 구현
 */

@Getter
@Builder
public class AttendanceStatResponseDto {

	private StudentInfo student;

	private AttendanceCounts counts;

	@Getter
	@Builder
	public static class StudentInfo {

		private Long id;

		private String name;

		private String email;

		public static StudentInfo from(Student student) {
			return StudentInfo.builder()
					.id(student.getId())
					.name(student.getName())
					.email(student.getEmail())
					.build();
		}

	}

	@Getter
	@Builder
	public static class AttendanceCounts {

		private int presentCount;    // 출석

		private int lateCount;       // 지각

		private int sickLeaveCount;  // 병결

		private int absentCount;     // 결석

		private int totalDays;       // 전체 수업일수

		private double attendanceRate; // 출석률 추가

	}

}
