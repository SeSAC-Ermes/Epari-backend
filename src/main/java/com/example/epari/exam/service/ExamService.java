package com.example.epari.exam.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.epari.course.domain.Course;
import com.example.epari.course.repository.CourseRepository;
import com.example.epari.course.repository.CourseStudentRepository;
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
import com.example.epari.global.validator.ExamQuestionValidator;
import com.example.epari.user.domain.Student;
import com.example.epari.user.repository.StudentRepository;

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

	private final ScoreCalculator scoreCalculator; 

	private final StudentRepository studentRepository;

	private final ExamStatusService examStatusService;

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
			if (status != null && !examStatusService.matchesStatus(exam, status, now)) {
				continue;  // status 필터링
			}

			if (role.contains("INSTRUCTOR")) {
				List<ExamResult> results = examResultRepository.findByExamId(exam.getId());
				ExamStatistics statistics = scoreCalculator.calculateExamStatistics(results, exam.getCourse().getId());
				ExamSummaryDto summaryDto = ExamSummaryDto.forInstructor(exam, statistics);
				examStatusService.categorizeExam(exam, summaryDto, now, scheduledExams, inProgressExams, completedExams);
			} else {
				// 학생의 경우 ExamResult가 없으면 새로운 시험으로 처리
				Optional<ExamResult> resultOptional = examResultRepository.findByExamIdAndStudentEmail(exam.getId(),
						email);
				ExamSummaryDto summaryDto;

				if (resultOptional.isPresent()) {
					summaryDto = ExamSummaryDto.forStudent(exam, resultOptional.get());
				} else {
					summaryDto = ExamSummaryDto.forNewExam(exam);
				}

				examStatusService.categorizeExam(exam, summaryDto, now, scheduledExams, inProgressExams, completedExams);
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
		Student student = studentRepository.findByEmail(studentEmail)
				.orElseThrow(() -> new BusinessBaseException(ErrorCode.STUDENT_NOT_FOUND));

		Exam exam = examQuestionValidator.validateExamAccess(courseId, examId);

		// 수강생 확인
		if (!courseStudentRepository.existsByCourseIdAndStudentId(courseId, student.getId())) {
			throw new BusinessBaseException(ErrorCode.UNAUTHORIZED_COURSE_ACCESS);
		}

		examQuestionValidator.validateExamTime(exam);
		examQuestionValidator.validateNotAlreadyStarted(examId, studentEmail);

		ExamResult examResult = ExamResult.builder().exam(exam).student(student).build();

		examResultRepository.save(examResult);
		return ExamStatusService.createExamSubmissionStatusDto(exam, examResult);
	}

	// 시험 조회
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

	// 만료된 시험 조회
	List<Exam> findExpiredExams() {
		LocalDateTime now = LocalDateTime.now();
		return examRepository.findByStatusIn(Arrays.asList(ExamStatus.SCHEDULED, ExamStatus.IN_PROGRESS))
				.stream()
				.filter(exam -> {
					LocalDateTime endTime = exam.getExamDateTime().plusMinutes(exam.getDuration());
					return now.isAfter(endTime);
				})
				.collect(Collectors.toList());
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
