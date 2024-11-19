package com.example.epari.course.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.epari.admin.dto.CourseSearchResponseDTO;
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
			+ "INNER JOIN CourseStudent cs ON c.id = cs.course.id "
			+ "WHERE cs.student.id = :studentId")
	List<Course> findAllByStudentId(@Param("studentId") Long studentId);

	/**
	 * 강사 id를 받아 강의 테이블에서 일치하는지 확인 강의 조회
	 */
	@Query("SELECT c FROM Course c "
			+ "WHERE c.instructor.id = :instructorId")
	List<Course> findAllByInstructorId(@Param("instructorId") Long instructorId);

	/**
	 * 주어진 강의 ID와 강사 이메일로 해당 강의가 해당 강사의 강의인지 확인합니다.
	 */
	@Query("""
			SELECT EXISTS (
			    SELECT 1
			    FROM Course c
			    WHERE c.id = :courseId
			    AND c.instructor.email = :instructorEmail
			)
			""")
	boolean existsByCourseIdAndInstructorEmail(
			@Param("courseId") Long courseId,
			@Param("instructorEmail") String instructorEmail);

	/**
	 * 주어진 강의 ID와 학생 이메일로 해당 강의를 수강중인 학생인지 확인합니다.
	 */
	@Query("""
			SELECT EXISTS (
			    SELECT 1
			    FROM Course c
			    JOIN CourseStudent cs ON c.id = cs.course.id
			    WHERE c.id = :courseId
			    AND cs.student.email = :studentEmail
			)
			""")
	boolean existsByCourseIdAndStudentEmail(
			@Param("courseId") Long courseId,
			@Param("studentEmail") String studentEmail);

	/**
	 * 키워드를 기반으로 강의 정보를 DTO로 직접 조회하는 쿼리 메서드
	 * - 키워드가 없으면 전체 조회
	 * - 필요한 컬럼만 선택적으로 조회
	 * - new 연산자로 DTO 직접 매핑
	 */
	@Query("""
			    SELECT new com.example.epari.admin.dto.CourseSearchResponseDTO(
			        c.id,
			        c.name,
			        i.name
			    )
			    FROM Course c
			    LEFT JOIN c.instructor i
			    WHERE (:keyword IS NULL OR
			           :keyword = '' OR
			           LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
			    ORDER BY c.name ASC
			""")
	List<CourseSearchResponseDTO> searchCoursesWithDTO(@Param("keyword") String keyword);

}
