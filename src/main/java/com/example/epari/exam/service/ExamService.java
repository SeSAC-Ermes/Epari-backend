package com.example.epari.exam.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.epari.exam.domain.Exam;
import com.example.epari.exam.dto.request.ExamRequestDto;
import com.example.epari.exam.repository.ExamRepository;
import com.example.epari.lecture.domain.Lecture;
import com.example.epari.lecture.repository.LectureRepository;

import lombok.RequiredArgsConstructor;

/**
 * 시험 관련 비즈니스 로직을 처리하는 Service 클래스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExamService {

	private final ExamRepository examRepository;

	private final LectureRepository lectureRepository;

	@Transactional
	public Long createExam(Long lectureId, ExamRequestDto requestDto) {
		// 강의 존재여부 확인
		Lecture lecture = lectureRepository.findById(lectureId)
				.orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

		Exam exam = Exam.builder()
				.title(requestDto.getTitle())
				.examDateTime(requestDto.getExamDateTime())
				.duration(requestDto.getDuration())
				.totalScore(requestDto.getTotalScore())
				.description(requestDto.getDescription())
				.lecture(lecture)
				.build();

		return examRepository.save(exam).getId();
	}

}
