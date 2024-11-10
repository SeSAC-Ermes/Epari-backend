package com.example.epari.exam.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.epari.exam.domain.Exam;
import com.example.epari.exam.dto.request.ExamRequestDto;
import com.example.epari.exam.dto.response.ExamResponseDto;
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

	// 시험 생성
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

	// 특정 강의의 시험 조회
	public List<ExamResponseDto> getExamsByLecture(Long lectureId) {
		// 강의 존재여부 확인
		if (!lectureRepository.existsById(lectureId)) {
			throw new IllegalArgumentException("강의를 찾을 수 없습니다. ID: " + lectureId);
		}

		// 시험 목록 조회 및 DTO 반환
		return examRepository.findByLectureId(lectureId).stream()
				.map(ExamResponseDto::fromExam)
				.collect(Collectors.toList());

	}

	// 특정 강의에 속한 시험 상세 조회
	public ExamResponseDto getExam(Long lectureId, Long id) {
		if (!lectureRepository.existsById(lectureId)) {
			throw new IllegalArgumentException("강의를 찾을 수 없습니다. ID: " + lectureId);
		}

		Exam exam = examRepository.findByLectureIdAndId(lectureId, id)
				.orElseThrow(() -> new IllegalArgumentException("시험을 찾을 수 없습니다." + id));

		return ExamResponseDto.fromExam(exam);
	}

	// 특정 강의에 속한 시험 수정
	public ExamResponseDto updateExam(Long lectureId, Long id, ExamRequestDto requestDto) {
		if (!lectureRepository.existsById(lectureId)) {
			throw new IllegalArgumentException("강의를 찾을 수 없습니다. ID: " + lectureId);
		}

		Exam exam = examRepository.findByLectureIdAndId(lectureId, id)
				.orElseThrow(() -> new IllegalArgumentException("시험을 찾을 수 없습니다." + id));

		exam.updateExam(
				requestDto.getTitle(),
				requestDto.getExamDateTime(),
				requestDto.getDuration(),
				requestDto.getTotalScore(),
				requestDto.getDescription()
		);

		return ExamResponseDto.fromExam(exam);
	}

}
