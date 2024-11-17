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
 * 시험 관련 비즈니스 로직을 처리하는 Service 클래스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExamService {

	private final ExamRepository examRepository;

	private final CourseRepository courseRepository;

	private final CourseAccessValidator courseAccessValidator;

	private final ExamFinder examFinder;

	// 시험 생성
	@Transactional
	public Long createExam(Long courseId, ExamRequestDto requestDto, String instructorEmail) {
		courseAccessValidator.validateInstructorAccess(courseId, instructorEmail);

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

	// 시험 목록 조회
	public List<ExamResponseDto> getExams(Long courseId, String email, String role) {
		if (role.contains("ROLE_INSTRUCTOR")) {
			courseAccessValidator.validateInstructorAccess(courseId, email);
			return examRepository.findByInstructorEmail(email).stream()
					.map(ExamResponseDto::fromExam)
					.collect(Collectors.toList());
		} else {
			courseAccessValidator.validateStudentAccess(courseId, email);
			return examRepository.findByStudentEmail(email).stream()
					.map(ExamResponseDto::fromExam)
					.collect(Collectors.toList());
		}
	}

	// 시험 상세 조회
	public ExamResponseDto getExam(Long courseId, Long examId, String email, String role) {
		if (role.contains("ROLE_INSTRUCTOR")) {
			courseAccessValidator.validateInstructorAccess(courseId, email);
		} else {
			courseAccessValidator.validateStudentAccess(courseId, email);
		}

		Exam exam = examFinder.findExam(courseId, examId);
		return ExamResponseDto.fromExam(exam);
	}

	// 시험 수정
	@Transactional
	public ExamResponseDto updateExam(Long courseId, Long examId, ExamRequestDto requestDto, String instructorEmail) {
		courseAccessValidator.validateInstructorAccess(courseId, instructorEmail);

		Exam exam = examFinder.findExam(courseId, examId);
		exam.updateExam(
				requestDto.getTitle(),
				requestDto.getExamDateTime(),
				requestDto.getDuration(),
				requestDto.getTotalScore(),
				requestDto.getDescription()
		);

		return ExamResponseDto.fromExam(exam);
	}

	// 시험 삭제
	@Transactional
	public void deleteExam(Long courseId, Long examId, String instructorEmail) {
		courseAccessValidator.validateInstructorAccess(courseId, instructorEmail);

		Exam exam = examFinder.findExam(courseId, examId);
		examRepository.delete(exam);
	}

}
