package com.example.epari.exam.service;

import org.springframework.stereotype.Component;

import com.example.epari.exam.domain.Exam;
import com.example.epari.exam.repository.ExamRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ExamFinder {

	private final ExamRepository examRepository;

	public Exam findExam(Long courseId, Long examId) {
		return examRepository.findByCourseIdAndId(courseId, examId)
				.orElseThrow(() -> new IllegalArgumentException("시험을 찾을 수 없습니다."));
	}

}
