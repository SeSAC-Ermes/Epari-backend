package com.example.epari.course.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.epari.course.domain.Course;

/**
 * 강의 Repository 인터페이스
 * JPA를 사용해 강의 Entity의 데이터베이스 연산을 담당합니다.
 */
public interface CourseRepository extends JpaRepository<Course, Long> {

	/**
	 * 학생 id를 받아 강의 id 와 학생 id 일치로 조회 강의 조회
	 */
	@Query("SELECT c FROM Course c "
			+ "INNER JOIN FETCH c.instructor i "
			+ "LEFT JOIN FETCH i.profileImage "
			+ "INNER JOIN CourseStudent cs ON c.id = cs.course.id "
			+ "WHERE cs.student.id = :studentId")
	List<Course> findAllByStudentId(@Param("studentId") Long studentId);

	/**
	 * 강사 id를 받아 강의 테이블에서 일치하는지 확인 강의 조회
	 */
	@Query("SELECT c FROM Course c "
			+ "INNER JOIN FETCH c.instructor i "
			+ "LEFT JOIN FETCH i.profileImage "
			+ "WHERE c.instructor.id = :instructorId")
	List<Course> findAllByInstructorId(@Param("instructorId") Long instructorId);

	/**
	 * 강사의 강의 접근 권한 확인
	 */
	@Query("SELECT EXISTS (SELECT 1 FROM Course c WHERE c.id = :courseId AND c.instructor.id = :instructorId)")
	boolean existsByCourseIdAndInstructorId(@Param("courseId") Long courseId, @Param("instructorId") Long instructorId);

	/**
	 * 학생의 강의 접근 권한 확인
	 */
	@Query("SELECT EXISTS (SELECT 1 FROM Course c JOIN c.courseStudents cs WHERE c.id = :courseId AND cs.student.id = :studentId)")
	boolean existsByCourseIdAndStudentId(@Param("courseId") Long courseId, @Param("studentId") Long studentId);

	@Query("SELECT c FROM Course c " +
			"INNER JOIN FETCH c.instructor i " +
			"WHERE c.id = :courseId")
	Optional<Course> findByIdWithInstructor(@Param("courseId") Long courseId);

}
