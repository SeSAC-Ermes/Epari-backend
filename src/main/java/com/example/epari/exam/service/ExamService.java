package com.example.epari.exam.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.epari.course.domain.Course;
import com.example.epari.course.repository.CourseRepository;
import com.example.epari.course.repository.CourseStudentRepository;
import com.example.epari.exam.domain.Exam;
import com.example.epari.exam.domain.ExamResult;
import com.example.epari.exam.dto.common.ExamStatistics;
import com.example.epari.exam.dto.request.ExamRequestDto;
import com.example.epari.exam.dto.response.ExamListResponseDto;
import com.example.epari.exam.dto.response.ExamResponseDto;
import com.example.epari.exam.dto.response.ExamSummaryDto;
import com.example.epari.exam.repository.ExamRepository;
import com.example.epari.exam.repository.ExamResultRepository;
import com.example.epari.global.common.enums.ExamStatus;
import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;
import com.example.epari.global.validator.CourseAccessValidator;
import com.example.epari.global.validator.ExamQuestionValidator;

import lombok.RequiredArgsConstructor;

/**
 * 시험 관련 비즈니스 로직을 처리하는 Service 클래스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExamService {

	private final ExamRepository examRepository;

	private final ExamResultRepository examResultRepository;

	private final CourseRepository courseRepository;

	private final CourseAccessValidator courseAccessValidator;

	private final CourseStudentRepository courseStudentRepository;

	private final ExamQuestionValidator examQuestionValidator;

	private boolean matchesStatus(Exam exam, ExamStatus status, LocalDateTime now) {
		return switch (status) {
			case SCHEDULED -> exam.isBeforeExam();
			case IN_PROGRESS -> exam.isDuringExam();
			case SUBMITTED, GRADING, GRADED, COMPLETED -> exam.isAfterExam();
			default -> false;
		};
	}

	private void categorizeExam(Exam exam, ExamSummaryDto summaryDto, LocalDateTime now,
			List<ExamSummaryDto> scheduledExams,
			List<ExamSummaryDto> inProgressExams,
			List<ExamSummaryDto> completedExams) {

		if (exam.isBeforeExam()) {
			scheduledExams.add(summaryDto);
		} else if (exam.isDuringExam()) {
			inProgressExams.add(summaryDto);
		} else {
			completedExams.add(summaryDto);
		}
	}

	private ExamStatistics calculateExamStatistics(Exam exam) {
		List<ExamResult> results = examResultRepository.findByExamId(exam.getId());

		return ExamStatistics.builder()
				.totalStudentCount(courseStudentRepository.countByCourseId(exam.getCourse().getId()))
				.submittedStudentCount((int)results.stream()
						.filter(r -> r.getStatus() == ExamStatus.SUBMITTED)
						.count())
				.averageScore(results.stream()
						.mapToInt(ExamResult::getEarnedScore)
						.average()
						.orElse(0.0))
				.build();
	}

	// 시험 목록 조회
	public ExamListResponseDto getExams(Long courseId, ExamStatus status, String email, String role) {
		Course course = courseRepository.findById(courseId)
				.orElseThrow(() -> new BusinessBaseException(ErrorCode.COURSE_NOT_FOUND));

		List<Exam> exams;

		// 권한에 따른 시험 목록 조회
		if (role.contains("INSTRUCTOR")) {
			exams = examRepository.findByInstructorEmail(email);
		} else {
			exams = examRepository.findByStudentEmail(email);
		}

		// 현재 시점 기준으로 시험 분류
		LocalDateTime now = LocalDateTime.now();

		List<ExamSummaryDto> scheduledExams = new ArrayList<>();
		List<ExamSummaryDto> inProgressExams = new ArrayList<>();
		List<ExamSummaryDto> completedExams = new ArrayList<>();

		for (Exam exam : exams) {
			if (status != null && !matchesStatus(exam, status, now)) {
				continue;  // status 필터링
			}

			if (role.contains("INSTRUCTOR")) {
				ExamStatistics statistics = calculateExamStatistics(exam);
				ExamSummaryDto summaryDto = ExamSummaryDto.forInstructor(exam, statistics);
				categorizeExam(exam, summaryDto, now, scheduledExams, inProgressExams, completedExams);
			} else {
				ExamResult result = examResultRepository.findByExamIdAndStudentEmail(exam.getId(), email)
						.orElseThrow(() -> new BusinessBaseException(ErrorCode.EXAM_RESULT_NOT_FOUND));
				ExamSummaryDto summaryDto = ExamSummaryDto.forStudent(exam, result);
				categorizeExam(exam, summaryDto, now, scheduledExams, inProgressExams, completedExams);
			}
		}

		return ExamListResponseDto.builder()
				.scheduledExams(scheduledExams)
				.inProgressExams(inProgressExams)
				.completedExams(completedExams)
				.build();
	}

	@Transactional(readOnly = true)
	public ExamResponseDto getExam(Long courseId, Long examId, String email, String role) {
		// 접근 권한 검증
		if (role.contains("ROLE_INSTRUCTOR")) {
			courseAccessValidator.validateInstructorAccess(courseId, email);
		} else {
			courseAccessValidator.validateStudentAccess(courseId, email);
		}

		// 시험 조회 (with fetch join questions)
		Exam exam = examRepository.findByIdWithQuestionsAndCourse(examId)
				.orElseThrow(() -> new BusinessBaseException(ErrorCode.EXAM_NOT_FOUND));

		// courseId 검증
		if (!exam.getCourse().getId().equals(courseId)) {
			throw new BusinessBaseException(ErrorCode.UNAUTHORIZED_EXAM_ACCESS);
		}

		// 권한별 응답 생성
		if (role.contains("ROLE_INSTRUCTOR")) {
			return ExamResponseDto.fromExamForInstructor(exam);
		} else {
			ExamResult result = examResultRepository
					.findByExamIdAndStudentEmail(examId, email)
					.orElse(null);
			return ExamResponseDto.fromExamForStudent(exam, result);
		}
	}

	// 시험 생성
	@Transactional
	public Long createExam(Long courseId, ExamRequestDto requestDto, String instructorEmail) {
		courseAccessValidator.validateInstructorAccess(courseId, instructorEmail);

		Course course = courseRepository.findById(courseId)
				.orElseThrow(() -> new BusinessBaseException(ErrorCode.COURSE_NOT_FOUND));

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

	// 시험 수정
	@Transactional
	public ExamResponseDto updateExam(Long courseId, Long examId, ExamRequestDto requestDto, String instructorEmail) {
		courseAccessValidator.validateInstructorAccess(courseId, instructorEmail);

		Exam exam = examQuestionValidator.validateExamAccess(courseId, examId);
		exam.updateExam(
				requestDto.getTitle(),
				requestDto.getExamDateTime(),
				requestDto.getDuration(),
				requestDto.getTotalScore(),
				requestDto.getDescription()
		);

		return ExamResponseDto.fromExamForInstructor(exam);
	}

	// 시험 삭제
	@Transactional
	public void deleteExam(Long courseId, Long examId, String instructorEmail) {
		courseAccessValidator.validateInstructorAccess(courseId, instructorEmail);

		Exam exam = examQuestionValidator.validateExamAccess(courseId, examId);
		examRepository.delete(exam);
	}

}
