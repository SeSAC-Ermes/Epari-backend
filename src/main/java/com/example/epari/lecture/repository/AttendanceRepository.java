package com.example.epari.lecture.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.epari.lecture.domain.Attendance;

/**
 * 출석 정보에 대한 데이터베이스 접근을 담당하는 레포지토리 인터페이스
 */
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

	/**
	 * 특정 강의의 특정 날짜 출석 데이터 전체 조회
	 */
	@Query("""
			SELECT a
			FROM Attendance a
			JOIN FETCH a.lectureStudent ls
			JOIN FETCH ls.student
			WHERE ls.lecture.id = :lectureId
			AND a.date = :date
			""")
	List<Attendance> findAllByLectureIdAndDate(
			@Param("lectureId") Long lectureId,
			@Param("date") LocalDate date
	);

	/**
	 * 특정 강의, 날짜의 특정 학생들의 출석 데이터 조회
	 */
	@Query("""
			SELECT a
			FROM Attendance a
			JOIN FETCH a.lectureStudent ls
			JOIN FETCH ls.student
			WHERE ls.lecture.id = :lectureId
			AND a.date = :date
			AND a.lectureStudent.id IN :studentIds
			""")
	List<Attendance> findByLectureIdAndDateAndStudentIds(
			@Param("lectureId") Long lectureId,
			@Param("date") LocalDate date,
			@Param("studentIds") List<Long> studentIds
	);

}
