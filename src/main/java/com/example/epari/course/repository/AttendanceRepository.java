package com.example.epari.course.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.epari.course.domain.Attendance;

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
			JOIN FETCH a.courseStudent ls
			JOIN FETCH ls.student
			WHERE ls.course.id = :courseId
			AND a.date = :date
			""")
	List<Attendance> findAllByCourseIdAndDate(
			@Param("courseId") Long courseId,
			@Param("date") LocalDate date
	);

	/**
	 * 특정 강의, 날짜의 특정 학생들의 출석 데이터 조회
	 */
	@Query("""
			SELECT a
			FROM Attendance a
			JOIN FETCH a.courseStudent ls
			JOIN FETCH ls.student s
			WHERE ls.course.id = :courseId
			AND a.date = :date
			AND s.id IN :studentIds
			""")
	List<Attendance> findByCourseIdAndDateAndStudentIds(
			@Param("courseId") Long courseId,
			@Param("date") LocalDate date,
			@Param("studentIds") List<Long> studentIds
	);

	/**
	 * 특정 강의의 특정 기간 출석 데이터 전체 조회
	 */
	@Query("""
			SELECT DISTINCT a
			FROM Attendance a
			JOIN FETCH a.courseStudent ls
			JOIN FETCH ls.student s
			WHERE ls.course.id = :courseId
			AND a.date BETWEEN :startDate AND :endDate
			ORDER BY s.name ASC, a.date ASC
			""")
	List<Attendance> findAllByCourseIdAndDateBetween(
			@Param("courseId") Long courseId,
			@Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate
	);

}
