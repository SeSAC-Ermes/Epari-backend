package com.example.epari.course.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.epari.course.domain.Attendance;
import com.example.epari.course.domain.CourseStudent;
import com.example.epari.course.dto.attendance.AttendanceResponseDto;
import com.example.epari.course.repository.AttendanceRepository;
import com.example.epari.course.repository.CourseRepository;
import com.example.epari.course.repository.CourseStudentRepository;

import lombok.RequiredArgsConstructor;

/**
 * 강의 출석 관리를 위한 비즈니스 로직을 처리하는 서비스 클래스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AttendanceService {

	private final AttendanceRepository attendanceRepository;

	private final CourseRepository courseRepository;

	private final CourseStudentRepository courseStudentRepository;

	/**
	 * 특정 강의의 특정 날짜 출석 현황을 조회
	 * 해당 날짜의 출석 데이터가 없는 경우, 수강생 전체의 출석 데이터를 새로 생성
	 */
	@Transactional
	public List<AttendanceResponseDto> getAttendances(Long courseId, String instructorEmail, LocalDate date) {
		validateInstructorAccess(courseId, instructorEmail);

		List<Attendance> attendances = attendanceRepository.findAllByCourseIdAndDate(courseId, date);

		if (attendances.isEmpty()) {
			attendances = initializeAttendances(courseId, date);
		}

		return attendances.stream()
				.map(AttendanceResponseDto::from)
				.toList();
	}

	/**
	 * 특정 강의의 특정 날짜에 대한 기본 출석 데이터를 생성
	 * 해당 강의를 수강하는 모든 학생들에 대해 결석 상태로 출석 데이터를 초기화
	 */
	private List<Attendance> initializeAttendances(Long courseId, LocalDate date) {
		List<CourseStudent> courseStudents =
				courseStudentRepository.findAllCourseStudentsByCourseId(courseId);

		List<Attendance> newAttendances = courseStudents.stream()
				.map(ls -> Attendance.builder()
						.courseStudent(ls)
						.date(date)
						.build())
				.toList();

		return attendanceRepository.saveAll(newAttendances);
	}

	/**
	 * 강사가 해당 강의에 대한 접근 권한이 있는지 검증
	 */
	private void validateInstructorAccess(Long courseId, String instructorEmail) {
		if (!courseRepository.existsByCourseIdAndInstructorEmail(courseId, instructorEmail)) {
			throw new IllegalArgumentException("해당 강의에 대한 접근 권한이 없습니다.");
		}
	}

}
