package com.example.epari.lecture.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.epari.lecture.domain.Attendance;
import com.example.epari.lecture.domain.LectureStudent;
import com.example.epari.lecture.dto.attendance.AttendanceResponseDto;
import com.example.epari.lecture.dto.attendance.AttendanceUpdateDto;
import com.example.epari.lecture.repository.AttendanceRepository;
import com.example.epari.lecture.repository.LectureRepository;
import com.example.epari.lecture.repository.LectureStudentRepository;

import lombok.RequiredArgsConstructor;

/**
 * 강의 출석 관리를 위한 비즈니스 로직을 처리하는 서비스 클래스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AttendanceService {

	private final AttendanceRepository attendanceRepository;

	private final LectureRepository lectureRepository;

	private final LectureStudentRepository lectureStudentRepository;

	/**
	 * 특정 강의의 특정 날짜 출석 현황을 조회
	 * 해당 날짜의 출석 데이터가 없는 경우, 수강생 전체의 출석 데이터를 새로 생성
	 */
	@Transactional
	public List<AttendanceResponseDto> getAttendances(Long lectureId, String instructorEmail, LocalDate date) {
		validateInstructorAccess(lectureId, instructorEmail);

		List<Attendance> attendances = attendanceRepository.findAllByLectureIdAndDate(lectureId, date);

		if (attendances.isEmpty()) {
			attendances = initializeAttendances(lectureId, date);
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
			Long lectureId,
			String instructorEmail,
			LocalDate date,
			List<AttendanceUpdateDto> updates
	) {
		// 강사 권한 검증
		validateInstructorAccess(lectureId, instructorEmail);

		// 수정할 학생 ID 목록 추출
		List<Long> studentIds = updates.stream()
				.map(AttendanceUpdateDto::getStudentId)
				.toList();

		// 수정할 출석 데이터만 조회
		List<Attendance> attendances = attendanceRepository.findByLectureIdAndDateAndStudentIds(
				lectureId,
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
						attendance -> attendance.getLectureStudent().getStudent().getId(),
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
	private List<Attendance> initializeAttendances(Long lectureId, LocalDate date) {
		List<LectureStudent> lectureStudents =
				lectureStudentRepository.findAllLectureStudentsByLectureId(lectureId);

		List<Attendance> newAttendances = lectureStudents.stream()
				.map(ls -> Attendance.builder()
						.lectureStudent(ls)
						.date(date)
						.build())
				.toList();

		return attendanceRepository.saveAll(newAttendances);
	}

	/**
	 * 강사가 해당 강의에 대한 접근 권한이 있는지 검증
	 */
	private void validateInstructorAccess(Long lectureId, String instructorEmail) {
		if (!lectureRepository.existsByLectureIdAndInstructorEmail(lectureId, instructorEmail)) {
			throw new IllegalArgumentException("해당 강의에 대한 접근 권한이 없습니다.");
		}
	}

}
