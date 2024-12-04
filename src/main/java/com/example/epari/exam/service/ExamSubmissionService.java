package com.example.epari.exam.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.epari.exam.domain.ExamQuestion;
import com.example.epari.exam.domain.ExamResult;
import com.example.epari.exam.domain.ExamScore;
import com.example.epari.exam.dto.common.AnswerSubmissionDto;
import com.example.epari.exam.repository.ExamQuestionRepository;
import com.example.epari.exam.repository.ExamResultRepository;
import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

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

	private final ExamResultRepository examResultRepository;

	private final ExamQuestionRepository examQuestionRepository;

	private final ExamResultService examResultService;

	private final ExamStatusService examStatusService;

	// 모든 문제 답안 제출 여부 검사
	public void validateAllQuestionsAnswered(ExamResult examResult) {
		int totalQuestions = examResult.getExam().getQuestions().size();
		int answeredQuestions = examResult.getScores().size();

		if (answeredQuestions < totalQuestions) {
			throw new BusinessBaseException(ErrorCode.EXAM_NOT_ALL_QUESTIONS_ANSWERED);
		}
	}
	
	// 답안 임시 저장
	public void saveAnswerTemporarily(Long courseId, Long examId, Long questionId, AnswerSubmissionDto answerDto,
			String studentEmail) {
		ExamResult examResult = examResultService.getExamResultInProgress(examId, studentEmail);
		examStatusService.validateExamTimeRemaining(examResult.getExam());

		// examId로 함께 검증
		ExamQuestion question = examQuestionRepository.findByExamIdAndId(examId, questionId)
				.orElseThrow(() -> new BusinessBaseException(ErrorCode.QUESTION_NOT_FOUND));

		// question이 해당 시험의 문제가 맞는지 한번 더 검증
		if (!question.getExam().getId().equals(examResult.getExam().getId())) {
			throw new BusinessBaseException(ErrorCode.UNAUTHORIZED_EXAM_ACCESS);
		}

		// 기존 임시 저장된 답안이 있는지 확인
		Optional<ExamScore> existingScore = examResult.getScores()
				.stream()
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
	public void submitAnswer(Long courseId, Long examId, Long questionId, AnswerSubmissionDto answerDto,
			String studentEmail) {
		ExamResult examResult = examResultService.getExamResultInProgress(examId, studentEmail);
		examStatusService.validateExamTimeRemaining(examResult.getExam());

		// examId로 함께 검증
		ExamQuestion question = examQuestionRepository.findByExamIdAndId(examId, questionId).orElseThrow(() -> {
			log.error("문제를 찾을 수 없음 - examId:{}, questionId:{}", examId, questionId);
			return new BusinessBaseException(ErrorCode.QUESTION_NOT_FOUND);
		});

		// question이 해당 시험의 문제가 맞는지 한번 더 검증
		if (!question.getExam().getId().equals(examResult.getExam().getId())) {
			throw new BusinessBaseException(ErrorCode.UNAUTHORIZED_EXAM_ACCESS);
		}

		// 기존 답안이 있는지 확인
		Optional<ExamScore> existingScore = examResult.getScores()
				.stream()
				.filter(score -> score.getQuestion().getId().equals(questionId))
				.findFirst();

		if (existingScore.isPresent()) {
			log.info("기존 답안 업데이트 - examId:{}, questionId:{}, answer:{}", examId, questionId, answerDto.getAnswer());
			// 기존 답안을 최종 제출로 변경
			existingScore.get().updateAnswer(answerDto.getAnswer());
			existingScore.get().markAsSubmitted();  // 임시저장 상태 해제
		} else {
			// 새로운 답안 생성
			log.info("새로운 답안 생성 - examId:{}, questionId:{}, answer:{}", examId, questionId, answerDto.getAnswer());
			ExamScore score = ExamScore.builder()
					.examResult(examResult)
					.question(question)
					.studentAnswer(answerDto.getAnswer())
					.temporary(false)  // 최종 제출
					.build();
			examResult.addScore(score);
		}

		log.info("답안 제출 완료 - examId:{}, questionId:{}", examId, questionId);
	}

	// 자동 제출
	@Value("${exam.max-duration-minutes:180}")  // application.properties에서 설정값을 가져옴
	private Integer maxExamDurationMinutes;

	// 만료된 시험 자동 제출
	@Scheduled(fixedRate = 60000) // 1분마다 실행
	@Transactional
	public void autoSubmitExpiredExams() {
		LocalDateTime baseTime = LocalDateTime.now().minusMinutes(maxExamDurationMinutes);
		List<ExamResult> expiredExams = examResultRepository.findExpiredExams(baseTime);

		expiredExams.stream().filter(result -> result.getExam().isAfterExam()).forEach(result -> {
			result.submit(true);
			log.info("Auto submitted exam: examId={}, studentId={}, submittedAt={}, remainingQuestions={}",
					result.getExam().getId(), result.getStudent().getId(), result.getSubmitTime(),
					result.getExam().getQuestions().size() - result.getSubmittedQuestionCount());
		});
	}

}
