package com.example.epari.course.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.epari.course.dto.curriculum.CurriculumResponseDto;
import com.example.epari.course.service.CurriculumService;

import lombok.RequiredArgsConstructor;

/**
 * 커리큘럼 관련 엔드포인트를 처리하는 컨트롤러 클래스
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/courses/{courseId}")
public class CurriculumController {

	private final CurriculumService curriculumService;

	/**
	 * 특정 강의 커리큘럼 조회
	 */
	@GetMapping("/curriculums")
	public List<CurriculumResponseDto> getCurriculums(@PathVariable("courseId") Long courseId) {
		return curriculumService.getCurriculumsByCourseId(courseId);
	}

}
