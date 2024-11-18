package com.example.epari.course.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.epari.course.domain.Attendance;
import com.example.epari.course.domain.Course;
import com.example.epari.course.dto.attendance.AttendanceStatResponseDto;
import com.example.epari.course.repository.AttendanceRepository;
import com.example.epari.course.repository.CourseRepository;
import com.example.epari.global.common.enums.AttendanceStatus;
import com.example.epari.global.exception.attendance.AttendanceFutureCourseException;
import com.example.epari.global.exception.attendance.AttendanceNotFoundException;
import com.example.epari.global.exception.course.CourseNotFoundException;
import com.example.epari.global.validator.CourseAccessValidator;
import com.example.epari.user.domain.Student;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AttendanceStatisticsService {

	private final AttendanceRepository attendanceRepository;

	private final CourseRepository courseRepository;

	private final CourseAccessValidator courseAccessValidator;

	public List<AttendanceStatResponseDto> getStudentAttendanceStats(Long courseId, String instructorEmail) {
		// 강사 권한 검증
		courseAccessValidator.validateInstructorAccess(courseId, instructorEmail);

		// 강의 정보 조회
		Course course = courseRepository.findById(courseId)
				.orElseThrow(() -> new CourseNotFoundException());

		// 날짜 유효성 검사
		validateCourseDate(course);

		// 조회 날짜 범위 설정
		LocalDate startDate = course.getStartDate();
		LocalDate endDate = determineEndDate(course);

		// 출석 데이터 조회 및 통계 생성
		return createAttendanceStatistics(courseId, startDate, endDate);
	}

	private void validateCourseDate(Course course) {
		LocalDate today = LocalDate.now();
		if (course.getStartDate().isAfter(today)) {
			log.warn("Future course access attempt - courseId: {}, startDate: {}",
					course.getId(), course.getStartDate());
			throw new AttendanceFutureCourseException();
		}
	}

	private LocalDate determineEndDate(Course course) {
		LocalDate today = LocalDate.now();
		LocalDate endDate = course.getEndDate();
		return today.isAfter(endDate) ? endDate : today;
	}

	private List<AttendanceStatResponseDto> createAttendanceStatistics(
			Long courseId, LocalDate startDate, LocalDate endDate) {

		// 출석 데이터 조회
		List<Attendance> allAttendances = attendanceRepository.findAllByCourseIdAndDateBetween(
				courseId, startDate, endDate);

		if (allAttendances == null || allAttendances.isEmpty()) {
			log.warn("No attendance data found - courseId: {}, period: {} ~ {}",
					courseId, startDate, endDate);
			throw new AttendanceNotFoundException();
		}

		// 학생별 출석 데이터 그룹화 및 통계 생성
		return allAttendances.stream()
				.collect(Collectors.groupingBy(
						attendance -> attendance.getCourseStudent().getStudent(),
						Collectors.collectingAndThen(
								Collectors.toList(),
								this::createStudentStatistics
						)))
				.values()
				.stream()
				.sorted(Comparator.comparing(dto -> dto.getStudent().getName()))
				.collect(Collectors.toList());
	}

	private AttendanceStatResponseDto createStudentStatistics(List<Attendance> attendances) {
		Student student = attendances.get(0).getCourseStudent().getStudent();

		AttendanceStatResponseDto.AttendanceCounts counts = AttendanceStatResponseDto.AttendanceCounts.builder()
				.presentCount((int)countByStatus(attendances, AttendanceStatus.PRESENT))
				.lateCount((int)countByStatus(attendances, AttendanceStatus.LATE))
				.sickLeaveCount((int)countByStatus(attendances, AttendanceStatus.SICK_LEAVE))
				.absentCount((int)countByStatus(attendances, AttendanceStatus.ABSENT))
				.totalDays(attendances.size())
				.build();

		return AttendanceStatResponseDto.builder()
				.student(AttendanceStatResponseDto.StudentInfo.from(student))
				.counts(counts)
				.build();
	}

	private long countByStatus(List<Attendance> attendances, AttendanceStatus status) {
		return attendances.stream()
				.filter(attendance -> attendance.getStatus() == status)
				.count();
	}

}
