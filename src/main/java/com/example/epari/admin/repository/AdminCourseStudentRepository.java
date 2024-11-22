package com.example.epari.admin.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.epari.admin.dto.CourseStudentResponseDTO;
import com.example.epari.course.domain.CourseStudent;

/**
 * 관리자 - 강의와 학생 관련 정보에 대한 데이터베이스 접근을 담당하는 레포지토리 인터페이스
 */
public interface AdminCourseStudentRepository extends JpaRepository<CourseStudent, Long> {

	/**
	 * 강의를 수강하는 학생 목록 조회
	 */
	@Query("""
			SELECT new com.example.epari.admin.dto.CourseStudentResponseDTO(
			    s.id,
			    s.name,
			    s.email,
			    cs.createdAt
			)
			FROM CourseStudent cs
			JOIN cs.student s
			WHERE cs.course.id = :courseId
			ORDER BY cs.createdAt DESC
			""")
	List<CourseStudentResponseDTO> findEnrolledStudentsByCourseId(Long courseId);

	void deleteByCourseIdAndStudentIdIn(Long courseId, Set<Long> studentIds);

	List<CourseStudent> findByCourseId(Long courseId);

}
