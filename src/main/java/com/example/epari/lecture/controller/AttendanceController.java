package com.example.epari.lecture.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.epari.global.annotation.CurrentUserEmail;
import com.example.epari.lecture.dto.attendance.AttendanceResponseDto;
import com.example.epari.lecture.service.AttendanceService;

import lombok.RequiredArgsConstructor;

/**
 * 강의 출석 관리를 위한 REST API 컨트롤러
 * 강사용 출석부 조회 및 관리 기능을 제공
 */
@RestController
@RequestMapping("/api/instructor/lectures/{lectureId}/attendances")
@RequiredArgsConstructor
public class AttendanceController {

	private final AttendanceService attendanceService;

	/**
	 * 특정 강의의 특정 날짜 출석 현황을 조회
	 */
	@GetMapping
	public ResponseEntity<List<AttendanceResponseDto>> getAttendances(
			@PathVariable Long lectureId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@CurrentUserEmail String email
	) {
		List<AttendanceResponseDto> responses = attendanceService.getAttendances(
				lectureId,
				email,
				date
		);

		return ResponseEntity.ok(responses);
	}

}
