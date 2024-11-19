package com.example.epari.course.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.epari.course.dto.attendance.AttendanceStatResponseDto;
import com.example.epari.course.service.AttendanceStatisticsService;
import com.example.epari.global.annotation.CurrentUserEmail;

import lombok.RequiredArgsConstructor;

/**
 * 설명: 강의별 학생 출석 통계를 조회하는 REST API 컨트롤러 구현
*/

@RestController
@RequestMapping("/api/courses/{courseId}/stats")
@RequiredArgsConstructor
public class AttendanceStatisticsController {

	private final AttendanceStatisticsService attendanceStatisticsService;

	@GetMapping
	@PreAuthorize("hasRole('INSTRUCTOR')")
	public ResponseEntity<List<AttendanceStatResponseDto>> getStudentAttendanceStats(
			@PathVariable Long courseId,
			@CurrentUserEmail String email) {

		List<AttendanceStatResponseDto> stats =
				attendanceStatisticsService.getStudentAttendanceStats(courseId, email);
		return ResponseEntity.ok(stats);
	}

}
