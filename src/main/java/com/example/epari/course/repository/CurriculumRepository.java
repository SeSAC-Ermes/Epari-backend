package com.example.epari.course.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.epari.course.domain.Curriculum;
import com.example.epari.course.dto.curriculum.CurriculumResponseDto;

/**
 * 커리큘럼 정보에 대한 데이터베이스 접근을 담당하는 레포지토리 인터페이스
 */
public interface CurriculumRepository extends JpaRepository<Curriculum, Long> {

	@Query("""
					SELECT new com.example.epari.course.dto.curriculum.CurriculumResponseDto(
						c.date,
						c.topic,
						c.description
					)
					FROM Curriculum c
					WHERE c.course.id = :courseId
					order by c.date asc
			""")
	List<CurriculumResponseDto> findAllByCourseId(@Param("courseId") Long courseId);

}
