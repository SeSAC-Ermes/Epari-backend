package com.example.epari.admin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.epari.course.domain.Curriculum;

/**
 * 관리자 - 커리큘럼 정보에 대한 데이터베이스 접근을 담당하는 레포지토리 인터페이스
 */
public interface AdminCurriculumRepository extends JpaRepository<Curriculum, Long> {

	/**
	 * 특정 강의에 속한 커리큘럼들을 ID 리스트를 기준으로 일괄 삭제
	 */
	@Query("DELETE FROM Curriculum c WHERE c.course.id = :courseId AND c.id IN :curriculumIds")
	@Modifying(clearAutomatically = true)
	void deleteByIdInAndCourseId(
			@Param("curriculumIds") List<Long> curriculumIds,
			@Param("courseId") Long courseId
	);

	/**
	 * 특정 강의에 속한 모든 커리큘럼 조회
	 */
	List<Curriculum> findByCourseId(Long courseId);

}
