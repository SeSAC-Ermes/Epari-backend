package com.example.epari.exam.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.epari.course.repository.CourseRepository;
import com.example.epari.exam.domain.Exam;
import com.example.epari.exam.dto.response.ExamResponseDto;
import com.example.epari.exam.repository.ExamRepository;

import lombok.RequiredArgsConstructor;

/**
 * 시험 관련 비즈니스 로직을 처리하는 Service 클래스
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ExamService {

	private final ExamRepository examRepository;

	private final CourseRepository courseRepository;

	// 특정 강의의 시험 조회
	@Transactional(readOnly = true)
	public List<ExamResponseDto> getExamByCourse(Long courseId) {
		// 강의 존재여부 확인
		if (!courseRepository.existsById(courseId)) {
			throw new IllegalArgumentException("강의를 찾을 수 없습니다. ID: " + courseId);
		}

		// 시험 목록 조회 및 DTO 반환
		return examRepository.findByCourseId(courseId).stream()
				.map(ExamResponseDto::fromExam)
				.collect(Collectors.toList());

	}

	// 특정 강의에 속한 시험 상세 조회
	@Transactional(readOnly = true)
	public ExamResponseDto getExam(Long courseId, Long id) {
		if (!courseRepository.existsById(courseId)) {
			throw new IllegalArgumentException("강의를 찾을 수 없습니다. ID: " + courseId);
		}

		Exam exam = examRepository.findByCourseIdAndId(courseId, id)
				.orElseThrow(() -> new IllegalArgumentException("시험을 찾을 수 없습니다." + id));

		return ExamResponseDto.fromExam(exam);
	}

}
