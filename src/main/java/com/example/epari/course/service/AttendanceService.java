package com.example.epari.course.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.epari.course.domain.Attendance;
import com.example.epari.course.domain.CourseStudent;
import com.example.epari.course.dto.attendance.AttendanceResponseDto;
import com.example.epari.course.dto.attendance.AttendanceUpdateDto;
import com.example.epari.course.repository.AttendanceRepository;
import com.example.epari.course.repository.CourseStudentRepository;
import com.example.epari.global.validator.CourseAccessValidator;

import lombok.RequiredArgsConstructor;

/**
 * 강의 출석 관리를 위한 비즈니스 로직을 처리하는 서비스 클래스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AttendanceService {

	private final AttendanceRepository attendanceRepository;

	private final CourseStudentRepository courseStudentRepository;

	private final CourseAccessValidator courseAccessValidator;

	/**
	 * 특정 강의의 특정 날짜 출석 현황을 조회
	 * 해당 날짜의 출석 데이터가 없는 경우, 수강생 전체의 출석 데이터를 새로 생성
	 */
	@Transactional
	public List<AttendanceResponseDto> getAttendances(Long courseId, String instructorEmail, LocalDate date) {
		courseAccessValidator.validateInstructorAccess(courseId, instructorEmail);

		List<Attendance> attendances = attendanceRepository.findAllByCourseIdAndDate(courseId, date);

		if (attendances.isEmpty()) {
			attendances = initializeAttendances(courseId, date);
		}

		return attendances.stream()
				.map(AttendanceResponseDto::from)
				.toList();
	}

	/**
	 * 특정 강의, 날짜의 학생 출석 상태를 변경
	 */
	@Transactional
	public void updateAttendances(
			Long courseId,
			String instructorEmail,
			LocalDate date,
			List<AttendanceUpdateDto> updates
	) {
		// 강사 권한 검증
		courseAccessValidator.validateInstructorAccess(courseId, instructorEmail);

		// 수정할 학생 ID 목록 추출
		List<Long> studentIds = updates.stream()
				.map(AttendanceUpdateDto::getStudentId)
				.toList();

		// 수정할 출석 데이터만 조회
		List<Attendance> attendances = attendanceRepository.findByCourseIdAndDateAndStudentIds(
				courseId,
				date,
				studentIds
		);

		// 출석 데이터가 하나라도 없다면 해당 날짜 출석부가 없는 것
		if (attendances.isEmpty()) {
			throw new IllegalArgumentException("해당 날짜의 출석 데이터가 존재하지 않습니다.");
		}

		// 조회된 데이터 수와 요청된 수가 다르다면 잘못된 요청
		if (attendances.size() != updates.size()) {
			throw new IllegalArgumentException("일부 학생의 출석 데이터를 찾을 수 없습니다.");
		}

		// studentId를 key로 하는 Map으로 변환하여 빠른 조회 가능하도록 함
		Map<Long, Attendance> attendanceMap = attendances.stream()
				.collect(Collectors.toMap(
						attendance -> attendance.getCourseStudent().getStudent().getId(),
						attendance -> attendance
				));

		// 업데이트 수행
		updates.forEach(update -> {
			Attendance attendance = attendanceMap.get(update.getStudentId());
			attendance.updateStatus(update.getStatus());
		});
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

}
