package com.example.epari.admin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.epari.admin.dto.AvailableStudentResponseDTO;
import com.example.epari.user.domain.Student;

/**
 * 관리자 - 학생 정보에 대한 데이터베이스 접근을 담당하는 레포지토리 인터페이스
 */
public interface AdminStudentRepository extends JpaRepository<Student, Long> {

	/**
	 * 특정 강의에 등록 가능한 학생 목록을 조회
	 * - 해당 강의에 이미 등록된 학생은 제외
	 * - keyword로 이름 또는 이메일 검색 가능
	 * - STUDENT 그룹에 속한 사용자만 조회
	 */
	@Query("""
			SELECT new com.example.epari.admin.dto.AvailableStudentResponseDTO(
			    s.id,
			    u.name,
			    u.email
			)
			FROM Student s
			JOIN BaseUser u ON u.id = s.id
			WHERE s.id NOT IN (
			    SELECT cs.student.id
			    FROM CourseStudent cs
			    WHERE cs.course.id = :courseId
			)
			AND (:keyword IS NULL OR :keyword = ''
			    OR LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
			    OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
			AND u.role = 'STUDENT'
			ORDER BY u.name ASC
			""")
	List<AvailableStudentResponseDTO> findAvailableStudentsForCourse(
			@Param("courseId") Long courseId,
			@Param("keyword") String keyword
	);

}
