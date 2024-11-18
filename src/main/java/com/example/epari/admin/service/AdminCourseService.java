package com.example.epari.admin.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.epari.admin.dto.CourseSearchResponseDTO;
import com.example.epari.course.repository.CourseRepository;

import lombok.RequiredArgsConstructor;

/**
 * 관리자기 강의를 관리하는 비즈니스 로직을 처리하는 서비스 클래스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCourseService {

	private final CourseRepository courseRepository;

	/**
	 * 키워드 기반 강의 검색 메서드
	 */
	public List<CourseSearchResponseDTO> searchCourses(String keyword) {
		return courseRepository.searchCoursesWithDTO(keyword);
	}

}
