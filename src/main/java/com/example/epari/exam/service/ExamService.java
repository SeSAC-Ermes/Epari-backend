package com.example.epari.exam.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.epari.course.domain.Course;
import com.example.epari.course.repository.CourseRepository;
import com.example.epari.exam.domain.Exam;
import com.example.epari.exam.domain.ExamResult;
import com.example.epari.exam.dto.common.ExamStatistics;
import com.example.epari.exam.dto.common.ExamSubmissionStatusDto;
import com.example.epari.exam.dto.request.ExamRequestDto;
import com.example.epari.exam.dto.response.ExamListResponseDto;
import com.example.epari.exam.dto.response.ExamResponseDto;
import com.example.epari.exam.dto.response.ExamSummaryDto;
import com.example.epari.exam.repository.ExamRepository;
import com.example.epari.exam.repository.ExamResultRepository;
import com.example.epari.exam.util.ScoreCalculator;
import com.example.epari.global.common.enums.ExamStatus;
import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;
import com.example.epari.global.validator.CourseAccessValidator;
import com.example.epari.global.validator.ExamBaseValidator;
import com.example.epari.global.validator.ExamStatusValidator;
import com.example.epari.global.validator.ExamTimeValidator;
import com.example.epari.user.domain.Instructor;
import com.example.epari.user.domain.Student;

import lombok.RequiredArgsConstructor;

/**
 * 시험 관련 비즈니스 로직을 처리하는 서비스 클래스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExamService {

	private final ExamRepository examRepository;

	private final ExamResultRepository examResultRepository;

	private final CourseRepository courseRepository;

	private final CourseAccessValidator courseAccessValidator;

	private final ExamBaseValidator examBaseValidator;

	private final ExamStatusValidator examStatusValidator;

	private final ScoreCalculator scoreCalculator;

	private final ExamStatusService examStatusService;

	private final ExamTimeValidator examTimeValidator;

	// 시험 생성
	@Transactional
	public Long createExam(Long courseId, ExamRequestDto requestDto, String instructorEmail) {
		Instructor instructor = courseAccessValidator.validateInstructorEmail(instructorEmail);
		courseAccessValidator.validateInstructorAccess(courseId, instructor.getId());

		examBaseValidator.validateExamDateTime(requestDto.getExamDateTime());
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

	// 시험 목록 조회
	public ExamListResponseDto getExams(Long courseId, ExamStatus status, String email, String role) {
		Course course = courseRepository.findById(courseId)
				.orElseThrow(() -> new BusinessBaseException(ErrorCode.COURSE_NOT_FOUND));

		List<Exam> exams;
		if (role.contains("INSTRUCTOR")) {
			Instructor instructor = courseAccessValidator.validateInstructorEmail(email);
			exams = examRepository.findByInstructorId(instructor.getId());
		} else {
			Student student = courseAccessValidator.validateStudentEmail(email);
			exams = examRepository.findByStudentId(student.getId());
		}

		// 현재 시점 기준으로 시험 분류
		LocalDateTime now = LocalDateTime.now();
		List<ExamSummaryDto> scheduledExams = new ArrayList<>();
		List<ExamSummaryDto> inProgressExams = new ArrayList<>();
		List<ExamSummaryDto> completedExams = new ArrayList<>();

		for (Exam exam : exams) {
			if (status != null && !examStatusValidator.matchesStatus(exam, status, now)) {
				continue;
			}

			if (role.contains("INSTRUCTOR")) {
				List<ExamResult> results = examResultRepository.findByExamId(exam.getId());
				ExamStatistics statistics = scoreCalculator.calculateExamStatistics(results, exam.getCourse().getId());
				ExamSummaryDto summaryDto = ExamSummaryDto.forInstructor(exam, statistics);
				examStatusService.categorizeExam(exam, summaryDto, now, scheduledExams, inProgressExams,
						completedExams);
			} else {
				Student student = courseAccessValidator.validateStudentEmail(email);
				Optional<ExamResult> resultOptional = examResultRepository.findByExamIdAndStudentId(exam.getId(),
						student.getId());
				ExamSummaryDto summaryDto = resultOptional.isPresent()
						? ExamSummaryDto.forStudent(exam, resultOptional.get())
						: ExamSummaryDto.forNewExam(exam);

				examStatusService.categorizeExam(exam, summaryDto, now, scheduledExams, inProgressExams,
						completedExams);
			}
		}

		return ExamListResponseDto.builder()
				.scheduledExams(scheduledExams)
				.inProgressExams(inProgressExams)
				.completedExams(completedExams)
				.build();
	}

	// 시험 응시
	public ExamSubmissionStatusDto startExam(Long courseId, Long examId, String studentEmail) {
		Student student = courseAccessValidator.validateStudentEmail(studentEmail);
		courseAccessValidator.validateStudentAccess(courseId, student.getId());
		examBaseValidator.validateExamAccess(examId, student.getId());
		examTimeValidator.validateExamPeriod(examId);
		examStatusValidator.validateExamStatus(examId, ExamStatus.SCHEDULED);

		Exam exam = examRepository.findById(examId)
				.orElseThrow(() -> new BusinessBaseException(ErrorCode.EXAM_NOT_FOUND));

		ExamResult examResult = ExamResult.builder()
				.exam(exam)
				.student(student)
				.build();

		examResultRepository.save(examResult);
		return ExamStatusService.createExamSubmissionStatusDto(exam, examResult);
	}

	// 시험 조회
	@Transactional(readOnly = true)
	public ExamResponseDto getExam(Long courseId, Long examId, String email, String role) {
		if (role.contains("ROLE_INSTRUCTOR")) {
			Instructor instructor = courseAccessValidator.validateInstructorEmail(email);
			courseAccessValidator.validateInstructorAccess(courseId, instructor.getId());
		} else {
			Student student = courseAccessValidator.validateStudentEmail(email);
			courseAccessValidator.validateStudentAccess(courseId, student.getId());
			examBaseValidator.validateExamAccess(examId, student.getId());
		}

		// 시험 조회 (with fetch join questions)
		Exam exam = examRepository.findByIdWithQuestionsAndCourse(examId)
				.orElseThrow(() -> new BusinessBaseException(ErrorCode.EXAM_NOT_FOUND));

		examBaseValidator.validateExamCourse(courseId, examId);

		// 권한별 응답 생성
		if (role.contains("ROLE_INSTRUCTOR")) {
			return ExamResponseDto.fromExamForInstructor(exam);
		} else {
			Student student = courseAccessValidator.validateStudentEmail(email);
			ExamResult result = examResultRepository.findByExamIdAndStudentId(examId, student.getId())
					.orElseThrow(() -> new BusinessBaseException(ErrorCode.EXAM_NOT_STARTED));
			return ExamResponseDto.fromExamForStudent(exam, result);
		}
	}

	// 시험 수정
	@Transactional
	public ExamResponseDto updateExam(Long courseId, Long examId, ExamRequestDto requestDto, String instructorEmail) {
		Instructor instructor = courseAccessValidator.validateInstructorEmail(instructorEmail);
		courseAccessValidator.validateInstructorAccess(courseId, instructor.getId());
		Exam exam = examBaseValidator.validateExamExists(examId);
		examBaseValidator.validateExamCourse(courseId, examId);
		examBaseValidator.validateExamDateTime(requestDto.getExamDateTime());

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
		Instructor instructor = courseAccessValidator.validateInstructorEmail(instructorEmail);
		courseAccessValidator.validateInstructorAccess(courseId, instructor.getId());
		Exam exam = examBaseValidator.validateExamExists(examId);
		examBaseValidator.validateExamCourse(courseId, examId);

		examRepository.delete(exam);
	}

}
