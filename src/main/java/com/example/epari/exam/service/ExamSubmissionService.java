package com.example.epari.exam.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

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
import com.example.epari.global.validator.ExamQuestionValidator;
import com.example.epari.user.domain.Student;
import com.example.epari.user.repository.StudentRepository;

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
    ExamResult examResult = getExamResultInProgress(examId, studentEmail);
    validateExamTimeRemaining(examResult.getExam());

    // examId로 함께 검증
    ExamQuestion question = examQuestionRepository
            .findByExamIdAndId(examId, questionId)
            .orElseThrow(() -> new BusinessBaseException(ErrorCode.QUESTION_NOT_FOUND));

    // question이 해당 시험의 문제가 맞는지 한번 더 검증
    if (!question.getExam().getId().equals(examResult.getExam().getId())) {
        throw new BusinessBaseException(ErrorCode.UNAUTHORIZED_EXAM_ACCESS);
    }

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

	public void submitAnswer(Long courseId, Long examId, Long questionId,
			AnswerSubmissionDto answerDto, String studentEmail) {
		ExamResult examResult = getExamResultInProgress(examId, studentEmail);
		validateExamTimeRemaining(examResult.getExam());

		// examId로 함께 검증
		ExamQuestion question = examQuestionRepository
				.findByExamIdAndId(examId, questionId)
				.orElseThrow(() -> new BusinessBaseException(ErrorCode.QUESTION_NOT_FOUND));

		// question이 해당 시험의 문제가 맞는지 한번 더 검증
		if (!question.getExam().getId().equals(examResult.getExam().getId())) {
			throw new BusinessBaseException(ErrorCode.UNAUTHORIZED_EXAM_ACCESS);
		}

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

	@Value("${exam.max-duration-minutes:180}")  // application.properties에서 설정값을 가져옴
	private Integer maxExamDurationMinutes;

	@Scheduled(fixedRate = 60000) // 1분마다 실행
	@Transactional
	public void autoSubmitExpiredExams() {
		LocalDateTime baseTime = LocalDateTime.now().minusMinutes(maxExamDurationMinutes);
		List<ExamResult> expiredExams = examResultRepository.findExpiredExams(baseTime);

		expiredExams.stream()
				.filter(result -> result.getExam().isAfterExam())
				.forEach(result -> {
					result.submit(true);
					log.info("Auto submitted exam: examId={}, studentId={}, submittedAt={}, remainingQuestions={}",
							result.getExam().getId(),
							result.getStudent().getId(),
							result.getSubmitTime(),
							result.getExam().getQuestions().size() - result.getSubmittedQuestionCount());
				});
	}

	// 시험 상태 조회
	@Transactional(readOnly = true)
	public ExamSubmissionStatusDto getSubmissionStatus(Long courseId, Long examId, String email) {
		Exam exam = examQuestionValidator.validateExamAccess(courseId, examId);
		ExamResult examResult = examResultRepository.findByExamIdAndStudentEmail(examId, email)
				.orElseThrow(() -> new BusinessBaseException(ErrorCode.EXAM_RESULT_NOT_FOUND));
		
		return ExamSubmissionStatusDto.builder()
				.examId(examId)
				.status(examResult.getStatus())
				.submittedQuestionCount(examResult.getSubmittedQuestionCount())
				.totalQuestionCount(exam.getQuestions().size())
				.build();
	}

	private void validateExamTimeRemaining(Exam exam) {
		if (!exam.isDuringExam()) {
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
				.submittedQuestionCount(examResult.getSubmittedQuestionCount())
				.totalQuestionCount(exam.getQuestions().size())
				.remainingTimeInMinutes(calculateRemainingTime(now, exam.getEndDateTime()))
				.submitTime(examResult.getSubmitTime())
				.build();
	}

	private int calculateRemainingTime(LocalDateTime now, LocalDateTime endTime) {
		if (now.isAfter(endTime)) {
			return 0;
		}
		return (int)Duration.between(now, endTime).toMinutes();
	}

}