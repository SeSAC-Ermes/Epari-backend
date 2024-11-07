package com.example.epari.lecture.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.epari.lecture.domain.Lecture;

/**
 * 강의 Repository 인터페이스
 * JPA를 사용해 강의 Entity의 데이터베이스 연산을 담당합니다.
 */
public interface LectureRepository extends JpaRepository<Lecture, Long> {

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
