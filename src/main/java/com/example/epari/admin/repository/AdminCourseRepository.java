package com.example.epari.admin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.epari.admin.dto.AdminCourseListResponseDto;
import com.example.epari.admin.dto.CourseSearchResponseDTO;
import com.example.epari.admin.dto.CurriculumDetailDto;
import com.example.epari.course.domain.Course;

/**
 * 관리자 - 강의 정보에 대한 데이터베이스 접근을 담당하는 레포지토리 인터페이스
 */
public interface AdminCourseRepository extends JpaRepository<Course, Long> {

	/**
	 * 모든 강의 목록 조회
	 */
	@Query("""
			SELECT new com.example.epari.admin.dto.AdminCourseListResponseDto(
			    c.id, c.name, c.classroom,
			    i.id, u.name,
			    c.startDate, c.endDate,
			    count(cs)
			)
			FROM Course c
			JOIN c.instructor i
			JOIN BaseUser u ON u.id = i.id
			LEFT JOIN c.courseStudents cs
			GROUP BY c.id, c.name, c.classroom, i.id, u.name, c.startDate, c.endDate
			ORDER BY c.startDate DESC
			""")
	List<AdminCourseListResponseDto> findAllWithStudentCount();

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

	/**
	 * 강의 ID로 강의 정보를 조회하며, 강사와 수강생 정보를 함께 페치 조인으로 가져옴
	 */
	@Query("""
			SELECT c
			FROM Course c
			JOIN FETCH c.instructor i
			LEFT JOIN FETCH c.courseStudents cs
			WHERE c.id = :courseId
			""")
	Optional<Course> findByIdWithInstructorAndStudents(@Param("courseId") Long courseId);

	/**
	 * 특정 강의의 커리큘럼을 DTO로 직접 조회하는 쿼리 메서드
	 */
	@Query("""
			SELECT new com.example.epari.admin.dto.CurriculumDetailDto(
			    cur.id, cur.date, cur.topic, cur.description)
			FROM Curriculum cur
			WHERE cur.course.id = :courseId
			ORDER BY cur.date
			""")
	List<CurriculumDetailDto> findCurriculumsByCourseId(@Param("courseId") Long courseId);

}
