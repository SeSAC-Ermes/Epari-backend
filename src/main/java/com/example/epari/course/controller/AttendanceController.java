package com.example.epari.course.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.epari.course.dto.attendance.AttendanceResponseDto;
import com.example.epari.course.dto.attendance.AttendanceUpdateDto;
import com.example.epari.course.service.AttendanceService;
import com.example.epari.global.annotation.CurrentUserEmail;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 강의 출석 관리를 위한 REST API 컨트롤러
 * 강사용 출석부 조회 및 관리 기능을 제공
 */
@RestController
@RequestMapping("/api/instructor/courses/{courseId}/attendances")
@RequiredArgsConstructor
public class AttendanceController {

	private final AttendanceService attendanceService;

	/**
	 * 특정 강의의 특정 날짜 출석 현황을 조회
	 */
	@GetMapping
	public ResponseEntity<List<AttendanceResponseDto>> getAttendances(
			@PathVariable Long courseId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@CurrentUserEmail String email
	) {
		List<AttendanceResponseDto> responses = attendanceService.getAttendances(
				courseId,
				email,
				date
		);

		return ResponseEntity.ok(responses);
	}

	/**
	 * 특정 날짜의 학생들 출석 상태를 일괄 수정
	 */
	@PatchMapping
	public ResponseEntity<Void> updateAttendances(
			@PathVariable Long courseId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@RequestBody @Valid List<AttendanceUpdateDto> request,
			@CurrentUserEmail String email
	) {
		attendanceService.updateAttendances(courseId, email, date, request);

		return ResponseEntity.ok().build();
	}

}
