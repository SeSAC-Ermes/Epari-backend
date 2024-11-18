package com.example.epari.exam.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.epari.course.repository.CourseStudentRepository;
import com.example.epari.exam.domain.Exam;
import com.example.epari.exam.domain.ExamQuestion;
import com.example.epari.exam.domain.ExamResult;
import com.example.epari.exam.domain.ExamScore;
import com.example.epari.exam.dto.common.AnswerSubmissionDto;
import com.example.epari.exam.dto.common.ExamSubmissionStatusDto;
import com.example.epari.exam.repository.ExamQuestionRepository;
import com.example.epari.exam.repository.ExamRepository;
import com.example.epari.exam.repository.ExamResultRepository;
import com.example.epari.global.common.enums.ExamStatus;
import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;
import com.example.epari.global.validator.CourseAccessValidator;
import com.example.epari.user.domain.Student;
import com.example.epari.user.repository.StudentRepository;
import com.example.epari.global.validator.ExamQuestionValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 시험 관련 비즈니스 로직을 처리하는 Service 클래스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ExamSubmissionService {

	private final ExamRepository examRepository;

	private final ExamResultRepository examResultRepository;

	private final ExamQuestionRepository examQuestionRepository;

	private final StudentRepository studentRepository;

	private final CourseAccessValidator courseAccessValidator;

	private final CourseStudentRepository courseStudentRepository;

	private final ExamQuestionValidator examQuestionValidator;

	// 시험 시작
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

		ExamResult examResult = ExamResult.builder()
				.exam(exam)
				.student(student)
				.build();

		examResultRepository.save(examResult);
		return createExamSubmissionStatusDto(exam, examResult);
	}

	// 답안 임시 저장
	public void saveAnswerTemporarily(Long courseId, Long examId, Long questionId,
			AnswerSubmissionDto answerDto, String studentEmail) {
		// 시험 결과 조회
		ExamResult examResult = getExamResultInProgress(examId, studentEmail);

		// 시험 시간 검증
		validateExamTimeRemaining(examResult.getExam());

		// 문제 조회
		ExamQuestion question = examQuestionRepository.findById(questionId)
				.orElseThrow(() -> new IllegalArgumentException("문제를 찾을 수 없습니다."));

		// 기존 임시 저장된 답안이 있는지 확인
		Optional<ExamScore> existingScore = examResult.getScores().stream()
				.filter(score -> score.getQuestion().getId().equals(questionId))
				.findFirst();

		if (existingScore.isPresent()) {
			// 기존 답안 업데이트
			existingScore.get().updateAnswer(answerDto.getAnswer());
		} else {
			// 새로운 답안 생성
			ExamScore score = ExamScore.builder()
					.examResult(examResult)
					.question(question)
					.studentAnswer(answerDto.getAnswer())
					.temporary(true)  // 임시저장 표시
					.build();
			examResult.addScore(score);
		}
	}

	// 답안 제출
	public void submitAnswer(Long courseId, Long examId, Long questionId,
			AnswerSubmissionDto answerDto, String studentEmail) {
		ExamResult examResult = getExamResultInProgress(examId, studentEmail);
		validateExamTimeRemaining(examResult.getExam());

		ExamQuestion question = examQuestionRepository.findById(questionId)
        .orElseThrow(() -> new BusinessBaseException(ErrorCode.QUESTION_NOT_FOUND));

		// 기존 답안이 있는지 확인
		Optional<ExamScore> existingScore = examResult.getScores().stream()
				.filter(score -> score.getQuestion().getId().equals(questionId))
				.findFirst();

		if (existingScore.isPresent()) {
			// 기존 답안을 최종 제출로 변경
			existingScore.get().updateAnswer(answerDto.getAnswer());
			existingScore.get().markAsSubmitted();  // 임시저장 상태 해제
		} else {
			// 새로운 답안 생성
			ExamScore score = ExamScore.builder()
					.examResult(examResult)
					.question(question)
					.studentAnswer(answerDto.getAnswer())
					.temporary(false)  // 최종 제출
					.build();
			examResult.addScore(score);
		}
	}

	// 시험 종료
	public void finishExam(Long courseId, Long examId, String studentEmail, boolean force) {
		ExamResult examResult = getExamResultInProgress(examId, studentEmail);
		if (!force) {
			validateAllQuestionsAnswered(examResult);
		}
		examResult.submit(force);
	}

	@Scheduled(fixedRate = 60000) // 1분마다 실행
	@Transactional
	public void autoSubmitExpiredExams() {
		// 현재 시간에서 가장 긴 시험 시간(예: 3시간)을 뺀 시간을 기준으로 조회
		LocalDateTime baseTime = LocalDateTime.now().minusMinutes(180);

		List<ExamResult> expiredExams = examResultRepository.findExpiredExams(baseTime);

		for (ExamResult examResult : expiredExams) {
			// 실제로 종료된 시험인지 다시 한번 체크
			if (examResult.getExam().isAfterExam()) {
				examResult.submit(true);
				log.info("시험 자동 제출 처리: examId={}, studentId={}",
						examResult.getExam().getId(),
						examResult.getStudent().getId());
			}
		}
	}

	// 시험 상태 조회
	@Transactional(readOnly = true)
	public ExamSubmissionStatusDto getSubmissionStatus(Long courseId, Long examId, String studentEmail) {
		ExamResult examResult = examResultRepository.findByExamIdAndStudentEmail(examId, studentEmail)
        .orElseThrow(() -> new BusinessBaseException(ErrorCode.EXAM_RESULT_NOT_FOUND));

		return createExamSubmissionStatusDto(examResult.getExam(), examResult);
	}

	private void validateExamTimeRemaining(Exam exam) {
		if (exam.isAfterExam()) {
			throw new BusinessBaseException(ErrorCode.EXAM_TIME_EXPIRED);
		}
	}

	private void validateAllQuestionsAnswered(ExamResult examResult) {
		int totalQuestions = examResult.getExam().getQuestions().size();
		int answeredQuestions = examResult.getScores().size();
	
		if (answeredQuestions < totalQuestions) {
			throw new BusinessBaseException(ErrorCode.EXAM_NOT_ALL_QUESTIONS_ANSWERED);
		}
	}

	// Helper 메서드
	private ExamResult getExamResultInProgress(Long examId, String studentEmail) {
		return examResultRepository.findByExamIdAndStudentEmail(examId, studentEmail)
				.filter(result -> result.getStatus() == ExamStatus.IN_PROGRESS)
				.orElseThrow(() -> new BusinessBaseException(ErrorCode.EXAM_NOT_IN_PROGRESS));
	}

	private ExamSubmissionStatusDto createExamSubmissionStatusDto(Exam exam, ExamResult examResult) {
		LocalDateTime now = LocalDateTime.now();
		return ExamSubmissionStatusDto.builder()
				.status(examResult.getStatus())
				.startTime(examResult.getSubmitTime())
				.endTime(exam.getEndDateTime())
				.submittedQuestionCount(examResult.getSubmittedQuestionCount())
				.totalQuestionCount(exam.getQuestions().size())
				.examEndTime(exam.getEndDateTime())
				.remainingTimeInMinutes(calculateRemainingTime(now, exam.getEndDateTime()))
				.build();
	}

	private int calculateRemainingTime(LocalDateTime now, LocalDateTime endTime) {
		if (now.isAfter(endTime)) {
			return 0;
		}
		return (int)Duration.between(now, endTime).toMinutes();
	}

}
