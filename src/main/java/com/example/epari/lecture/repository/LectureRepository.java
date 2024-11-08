package com.example.epari.lecture.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.epari.lecture.domain.Lecture;

/**
 * 강의 Repository 인터페이스
 * JPA를 사용해 강의 Entity의 데이터베이스 연산을 담당합니다.
 */
public interface LectureRepository extends JpaRepository<Lecture, Long> {

	/**
	 * 학생 id를 받아 강의 id 와 학생 id 일치로 조회 강의 조회
	 */
	@Query("SELECT l FROM Lecture l "
		   + "INNER JOIN LectureStudent ls ON l.id = ls.lecture.id "
		   + "WHERE ls.student.id = :studentId")
	List<Lecture> findAllByStudentId(@Param("studentId") Long studentId);

	/**
	 * 강사 id를 받아 강의 테이블에서 일치하는지 확인 강의 조회
	 */
	@Query("SELECT l FROM Lecture l "
		   + "WHERE l.instructor.id = :instructorId")
	List<Lecture> findAllByInstructorId(@Param("instructorId") Long instructorId);

	/**
	 * 주어진 강의 ID와 강사 이메일로 해당 강의가 해당 강사의 강의인지 확인합니다.
	 */
	@Query("""
			SELECT EXISTS (
			    SELECT 1
			    FROM Lecture l
			    WHERE l.id = :lectureId
			    AND l.instructor.email = :instructorEmail
			)
			""")
	boolean existsByLectureIdAndInstructorEmail(Long lectureId, String instructorEmail);

}
