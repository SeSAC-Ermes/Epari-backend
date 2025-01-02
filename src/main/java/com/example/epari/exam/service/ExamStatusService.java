package com.example.epari.exam.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.epari.exam.domain.Exam;
import com.example.epari.exam.domain.ExamResult;
import com.example.epari.exam.dto.common.ExamSubmissionStatusDto;
import com.example.epari.exam.dto.response.ExamSummaryDto;
import com.example.epari.exam.repository.ExamRepository;
import com.example.epari.exam.repository.ExamResultRepository;
import com.example.epari.global.common.enums.ExamStatus;
import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;
import com.example.epari.global.validator.CourseAccessValidator;
import com.example.epari.global.validator.ExamQuestionValidator;
import com.example.epari.global.validator.ExamTimeValidator;
import com.example.epari.user.domain.Student;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 시험 상태 서비스
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ExamStatusService {

	private final ExamRepository examRepository;

	private final ExamResultRepository examResultRepository;

	private final ExamGradingService gradingService;

	private final ExamResultService examResultService;

	private final ExamSubmissionService examSubmissionService;

	private final ExamQuestionValidator examQuestionValidator;

	private final ExamTimeValidator examTimeValidator;

	private final CourseAccessValidator courseAccessValidator;

	// 시험 분류
	// TODO: 시험 상태 웹소켓을 통한 실시간 변경 필요
	public void categorizeExam(Exam exam, ExamSummaryDto summaryDto, LocalDateTime now,
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

	// 시험 상태 조회
	@Transactional(readOnly = true)
	public ExamSubmissionStatusDto getSubmissionStatus(Long courseId, Long examId, String email) {
		// email -> id 변환
		Student student = courseAccessValidator.validateStudentEmail(email);

		Exam exam = examQuestionValidator.validateExamAccess(courseId, examId);
		ExamResult examResult = examResultRepository.findByExamIdAndStudentId(examId, student.getId())
				.orElseThrow(() -> new BusinessBaseException(ErrorCode.EXAM_RESULT_NOT_FOUND));

		return ExamSubmissionStatusDto.builder()
				.examId(examId)
				.status(examResult.getStatus())
				.submittedQuestionCount(examResult.getSubmittedQuestionCount())
				.totalQuestionCount(exam.getQuestions().size())
				.build();
	}

	// 현재 시험 제출 상태 DTO 생성
	public static ExamSubmissionStatusDto createExamSubmissionStatusDto(Exam exam, ExamResult examResult) {
		LocalDateTime now = LocalDateTime.now();
		return ExamSubmissionStatusDto.builder()
				.status(examResult.getStatus())
				.submittedQuestionCount(examResult.getSubmittedQuestionCount())
				.totalQuestionCount(exam.getQuestions().size())
				.remainingTimeInMinutes(calculateRemainingTime(now, exam.getEndDateTime()))
				.submitTime(examResult.getSubmitTime())
				.build();
	}

	// 남은 시간 계산
	public static int calculateRemainingTime(LocalDateTime now, LocalDateTime endTime) {
		if (now.isAfter(endTime)) {
			return 0;
		}
		return (int)Duration.between(now, endTime).toMinutes();
	}

	// 시험 시간 유효성 검사
	public void validateExamTimeRemaining(Long examId, String email) {
		// email -> id 변환
		Student student = courseAccessValidator.validateStudentEmail(email);

		ExamResult examResult = examResultRepository.findByExamIdAndStudentId(examId, student.getId())
				.orElseThrow(() -> new BusinessBaseException(ErrorCode.EXAM_RESULT_NOT_FOUND));

		examTimeValidator.validateExamTimeRemaining(examResult.getExam());
	}

	// 시험 종료 처리
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void processExamEnd(Exam exam) {
		// 시험 상태를 먼저 IN_PROGRESS로 변경
		if (exam.getStatus() == ExamStatus.SCHEDULED) {
			exam.updateStatus(ExamStatus.IN_PROGRESS);
			examRepository.save(exam);
		}

		// 1. 미제출자 강제 제출 처리
		List<ExamResult> inProgressResults = examResultRepository.findByExamIdAndStatus(exam.getId(),
				ExamStatus.IN_PROGRESS);

		for (ExamResult result : inProgressResults) {
			try {
				result.submit(true);
				examResultRepository.save(result);
				log.info("시험 자동 제출 처리 완료. examId={}, studentId={}", exam.getId(), result.getStudent().getId());
			} catch (Exception e) {
				log.error("시험 자동 제출 실패. examId={}, studentId={}", exam.getId(), result.getStudent().getId(), e);
				// 개별 실패는 전체 트랜잭션을 롤백하지 않음
			}
		}

		// 2. 시험 상태를 채점중으로 변경
		exam.updateStatus(ExamStatus.GRADING);
		examRepository.save(exam);

		try {
			// 3. 채점 프로세스 시작
			startGradingProcess(exam);
		} catch (Exception e) {
			log.error("채점 실패. examId={}", exam.getId(), e);
			// 채점 실패해도 시험 종료 처리는 커밋
		}

		log.info("시험 종료 처리 완료. examId={}", exam.getId());
	}

	// 채점 프로세스 시작
	private void startGradingProcess(Exam exam) {
		List<ExamResult> submittedResults = examResultRepository.findByExamIdAndStatus(exam.getId(),
				ExamStatus.SUBMITTED);

		// GradingService의 채점 로직 활용
		for (ExamResult result : submittedResults) {
			try {
				gradingService.processGrading(result);
				log.info("채점 완료. examId={}, studentId={}, score={}", exam.getId(), result.getStudent().getId(),
						result.getEarnedScore());
			} catch (Exception e) {
				log.error("채점 실패. examId={}, studentId={}", exam.getId(), result.getStudent().getId(), e);
			}
		}

		// 채점 완료 후 시험 상태 업데이트
		exam.updateStatus(ExamStatus.GRADED);
		examRepository.save(exam);

		// 최종 종료 상태로 변경
		finalizeExam(exam);
	}

	// 시험 종료 처리
	private void finalizeExam(Exam exam) {
		exam.updateStatus(ExamStatus.COMPLETED);
		examRepository.save(exam);
		log.info("시험 종료 처리 완료. examId={}", exam.getId());
	}

	// 학생 시험 상태에 따라 시험 결과 제출
	public void finishExam(Long courseId, Long examId, String email, boolean force) {
		Student student = courseAccessValidator.validateStudentEmail(email);

		ExamResult examResult = examResultService.getExamResultInProgress(examId, student.getId());
		if (!force) {
			examSubmissionService.validateAllQuestionsAnswered(examResult);
		}
		examResult.submit(force);
	}

	public void validateExamInProgress(Exam exam) {
		examTimeValidator.validateExamTime(exam.getId());
	}

}
