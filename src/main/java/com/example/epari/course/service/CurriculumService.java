package com.example.epari.course.service;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.epari.course.dto.curriculum.CurriculumResponseDto;
import com.example.epari.course.repository.CurriculumRepository;

import lombok.RequiredArgsConstructor;

/**
 * 커리큘럼 관련 비즈니스 로직을 처리하는 서비스 클래스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CurriculumService {

	private final CurriculumRepository curriculumRepository;

	/**
	 * 특정 강의 커리큘럼 조회
	 */
	@Cacheable(
			value = "curriculums",
			key = "#courseId",
			unless = "#result.isEmpty()"
	)
	public List<CurriculumResponseDto> getCurriculumsByCourseId(Long courseId) {
		return curriculumRepository.findAllByCourseId(courseId);
	}

}
