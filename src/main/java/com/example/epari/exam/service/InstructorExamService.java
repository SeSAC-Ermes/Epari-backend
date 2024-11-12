package com.example.epari.exam.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.epari.course.domain.Course;
import com.example.epari.course.repository.CourseRepository;
import com.example.epari.exam.domain.Exam;
import com.example.epari.exam.dto.request.ExamRequestDto;
import com.example.epari.exam.dto.response.ExamResponseDto;
import com.example.epari.exam.repository.ExamRepository;
import com.example.epari.global.validator.CourseAccessValidator;

import lombok.RequiredArgsConstructor;

/**
 * 강사의 시험 조회 관련 비즈니스 로직을 처리하는 Service 클래스
 */
@Service
@RequiredArgsConstructor
@Transactional
public class InstructorExamService {

	private final ExamRepository examRepository;

	private final CourseRepository courseRepository;

	private final CourseAccessValidator courseAccessValidator;

	@Transactional(readOnly = true)
	public List<ExamResponseDto> getExams(String email) {
		List<Exam> exams = examRepository.findByInstructorEmail(email);
		return exams.stream()
				.map(ExamResponseDto::fromExam)
				.collect(Collectors.toList());
	}

	// 특정 강의에 시험 생성
	public Long createExam(Long courseId, ExamRequestDto requestDto, String instructorEmail) {

		courseAccessValidator.validateInstructorAccess(courseId, instructorEmail);

		// 강의 존재여부 확인
		Course course = courseRepository.findById(courseId)
				.orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

		Exam exam = Exam.builder()
				.title(requestDto.getTitle())
				.examDateTime(requestDto.getExamDateTime())
				.duration(requestDto.getDuration())
				.totalScore(requestDto.getTotalScore())
				.description(requestDto.getDescription())
				.course(course)
				.build();

		return examRepository.save(exam).getId();
	}

	// 특정 강의에 속한 시험 수정
	public ExamResponseDto updateExam(Long courseId, Long id, ExamRequestDto requestDto, String instructorEmail) {

		courseAccessValidator.validateInstructorAccess(courseId, instructorEmail);

		if (!courseRepository.existsById(courseId)) {
			throw new IllegalArgumentException("강의를 찾을 수 없습니다. ID: " + courseId);
		}

		Exam exam = examRepository.findByCourseIdAndId(courseId, id)
				.orElseThrow(() -> new IllegalArgumentException("시험을 찾을 수 없습니다. ID:" + id));

		exam.updateExam(
				requestDto.getTitle(),
				requestDto.getExamDateTime(),
				requestDto.getDuration(),
				requestDto.getTotalScore(),
				requestDto.getDescription()
		);

		return ExamResponseDto.fromExam(exam);
	}

	// 특정 강의에 속한 시험 삭제
	public void deleteExam(Long courseId, Long id, String instructorEmail) {

		courseAccessValidator.validateInstructorAccess(courseId, instructorEmail);

		if (!courseRepository.existsById(courseId)) {
			throw new IllegalArgumentException("강의를 찾을 수 없습니다. ID: " + courseId);
		}

		Exam exam = examRepository.findByCourseIdAndId(courseId, id)
				.orElseThrow(() -> new IllegalArgumentException("시험을 찾을 수 없습니다. ID:" + id));

		examRepository.delete(exam);
	}

}
