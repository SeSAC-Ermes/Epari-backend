package com.example.epari.course.service;

import org.springframework.stereotype.Service;

import com.example.epari.global.common.enums.AttendanceStatus;

/**
 * 출석률 계산을 전담하는 서비스
 */
@Service
public class AttendanceCalculationService {

	// 출석 상태별 가중치 상수
	private static final double PRESENT_WEIGHT = 1.0;    // 출석 100%

	private static final double LATE_WEIGHT = 0.5;       // 지각 50%

	private static final double SICK_LEAVE_WEIGHT = 1.0; // 병결 100%

	private static final double ABSENT_WEIGHT = 0.0;     // 결석 0%

	/**
	 * 출석률을 계산
	 * @param presentCount 출석 횟수
	 * @param lateCount 지각 횟수
	 * @param sickLeaveCount 병결 횟수
	 * @param absentCount 결석 횟수
	 * @param totalDays 전체 수업일수
	 * @return 계산된 출석률 (소수점 첫째자리까지)
	 */
	public double calculateAttendanceRate(
			int presentCount,
			int lateCount,
			int sickLeaveCount,
			int absentCount,
			int totalDays
	) {
		if (totalDays == 0)
			return 0.0;

		double effectiveAttendance =
				(presentCount * PRESENT_WEIGHT) +
				(lateCount * LATE_WEIGHT) +
				(sickLeaveCount * SICK_LEAVE_WEIGHT) +
				(absentCount * ABSENT_WEIGHT);

		return Math.round((effectiveAttendance / totalDays) * 100 * 10) / 10.0;
	}

	/**
	 * 특정 출석 상태의 가중치 반환
	 * @param status 출석 상태
	 * @return 해당 상태의 가중치
	 */
	public double getWeightByStatus(AttendanceStatus status) {
		return switch (status) {
			case PRESENT -> PRESENT_WEIGHT;
			case LATE -> LATE_WEIGHT;
			case SICK_LEAVE -> SICK_LEAVE_WEIGHT;
			case ABSENT -> ABSENT_WEIGHT;
		};
	}

	/**
	 * 출석 인정 여부 확인 (출석률에 반영되는 상태인지)
	 * @param status 출석 상태
	 * @return 출석 인정 여부
	 */
	public boolean isAttendanceRecognized(AttendanceStatus status) {
		return getWeightByStatus(status) > 0;
	}

}
