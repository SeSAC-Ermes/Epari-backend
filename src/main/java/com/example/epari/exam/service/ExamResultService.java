package com.example.epari.exam.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.epari.exam.domain.ExamResult;
import com.example.epari.exam.dto.response.ExamResultResponseDto;
import com.example.epari.exam.repository.ExamResultRepository;
import com.example.epari.exam.util.ScoreCalculator;
import com.example.epari.global.exception.exam.ExamResultNotFoundException;
import com.example.epari.global.validator.CourseAccessValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 시험 결과 관련 비즈니스 로직을 처리하는 서비스 클래스
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ExamResultService {

	private final ExamResultRepository examResultRepository;

	private final CourseAccessValidator courseAccessValidator;

	private final ScoreCalculator scoreCalculator;

	// 강의의 모든 학생 시험 결과를 조회
	public List<ExamResultResponseDto> getCourseExamResults(Long courseId, String instructorEmail) {
		// 강사 권한 검증
		courseAccessValidator.validateInstructorAccess(courseId, instructorEmail);

		// 해당 강의의 모든 시험 결과를 조회
		List<ExamResult> examResults = examResultRepository.findAllByCourseId(courseId);
		if (examResults.isEmpty()) {
			log.warn("No exam results found for course ID: {}", courseId);
			throw new ExamResultNotFoundException();
		}

		// 학생별로 시험 결과를 그룹화
		Map<Long, List<ExamResult>> resultsByStudent = examResults.stream()
				.collect(Collectors.groupingBy(result -> result.getStudent().getId()));

		// 각 학생별 ExamResultResponseDto 생성
		return resultsByStudent.values().stream()
				.map(this::createExamResultResponse)
				.collect(Collectors.toList());
	}

	// 시험 결과 생성 응답
	private ExamResultResponseDto createExamResultResponse(List<ExamResult> examResults) {
		ExamResultResponseDto.StudentInfo studentInfo =
				ExamResultResponseDto.StudentInfo.from(examResults.get(0).getStudent());

		List<ExamResultResponseDto.ExamInfo> examInfos = examResults.stream()
				.map(result -> ExamResultResponseDto.ExamInfo.from(
						result,
						result.getEarnedScore()
				))
				.collect(Collectors.toList());

		double averageScore = scoreCalculator.calculateAverageScore(examResults);

		return ExamResultResponseDto.builder()
				.student(studentInfo)
				.examResults(examInfos)
				.averageScore(averageScore)
				.build();
	}

}
