package com.example.epari.exam.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.epari.course.domain.CourseStudent;
import com.example.epari.course.repository.CourseStudentRepository;
import com.example.epari.exam.domain.Exam;
import com.example.epari.exam.domain.ExamQuestion;
import com.example.epari.exam.domain.ExamResult;
import com.example.epari.exam.dto.common.ExamResultDetailDto;
import com.example.epari.exam.dto.common.ExamResultSummaryDto;
import com.example.epari.exam.dto.common.QuestionResultDto;
import com.example.epari.exam.dto.response.ExamResultResponseDto;
import com.example.epari.exam.repository.ExamRepository;
import com.example.epari.exam.repository.ExamResultRepository;
import com.example.epari.exam.util.ScoreCalculator;
import com.example.epari.global.common.enums.ExamStatus;
import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;
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

	private final ExamRepository examRepository;

	private final ExamResultRepository examResultRepository;

	private final CourseAccessValidator courseAccessValidator;

	private final ScoreCalculator scoreCalculator;

	private final CourseStudentRepository courseStudentRepository;

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

	// 모든 시험 결과 조회
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

	// 모든 시험의 학생별 결과 조회
	@Transactional(readOnly = true)
	public List<ExamResultSummaryDto> getExamResults(Long courseId, Long examId) {
		// 1. 해당 코스의 모든 수강생 목록 조회
		List<CourseStudent> courseStudents = courseStudentRepository.findAllCourseStudentsByCourseId(courseId);

		// 2. 시험 결과 조회
		List<ExamResult> submittedResults = examResultRepository.findByExamId(examId);

		// 3. 학생별 결과 매핑
		return courseStudents.stream().map(courseStudent -> {
			// 학생의 시험 결과 찾기
			ExamResult result = submittedResults.stream()
					.filter(r -> r.getStudent().getEmail().equals(courseStudent.getStudent().getEmail()))
					.findFirst()
					.orElse(null);

			// 결과가 없는 경우 (미제출) 기본값으로 DTO 생성
			if (result == null) {
				return ExamResultSummaryDto.builder()
						.studentName(courseStudent.getStudent().getName())
						.studentEmail(courseStudent.getStudent().getEmail())
						.status(ExamStatus.NOT_SUBMITTED)
						.totalScore(0)
						.submittedAt(null)  // 미제출이므로 null
						.build();
			}

			// 결과가 있는 경우 정상적으로 DTO 생성
			return ExamResultSummaryDto.from(result);
		}).collect(Collectors.toList());
	}

	// 시험 결과 상세 조회
	public ExamResultDetailDto getStudentExamResultById(Long resultId) {
		// 1. 시험 결과 조회
		ExamResult examResult = examResultRepository.findById(resultId)
				.orElseThrow(() -> new BusinessBaseException(ErrorCode.EXAM_RESULT_NOT_FOUND));

		// 2. 시험 정보 조회
		Exam exam = examRepository.findById(examResult.getExam().getId())
				.orElseThrow(() -> new BusinessBaseException(ErrorCode.EXAM_NOT_FOUND));

		// 3. 문제별 답안 조회 (ExamScore 사용)
		List<QuestionResultDto> questionResults = examResult.getScores().stream().map(score -> {
			ExamQuestion question = score.getQuestion();
			return QuestionResultDto.builder()
					.questionId(question.getId())
					.questionTitle(question.getQuestionText())
					.questionText(question.getQuestionText())
					.type(question.getType())  // ExamQuestionType enum 사용
					.score(question.getScore()) // 배점
					.earnedScore(score.getEarnedScore()) // 획득 점수
					.correctAnswer(question.getCorrectAnswer()) // 정답
					.studentAnswer(score.getStudentAnswer()) // 학생 답안
					.build();
		}).collect(Collectors.toList());

		return ExamResultDetailDto.builder()
				.examId(exam.getId())
				.examTitle(exam.getTitle())
				.startTime(examResult.getCreatedAt())
				.endTime(examResult.getSubmitTime())
				.status(examResult.getStatus())
				.totalScore(examResult.getEarnedScore())
				.questionResults(questionResults)
				.build();
	}

	// 진행중인 시험 결과 조회
	ExamResult getExamResultInProgress(Long examId, String studentEmail) {
		return examResultRepository.findByExamIdAndStudentEmail(examId, studentEmail)
				.filter(result -> result.getStatus() == ExamStatus.IN_PROGRESS)
				.orElseThrow(() -> new BusinessBaseException(ErrorCode.EXAM_NOT_IN_PROGRESS));
	}

}
