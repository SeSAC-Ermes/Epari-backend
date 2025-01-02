package com.example.epari.global.validator;

import org.springframework.stereotype.Component;

import com.example.epari.exam.domain.ExamResult;
import com.example.epari.exam.repository.ExamResultRepository;
import com.example.epari.global.common.enums.ExamStatus;
import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * 시험 제출 관련 검증을 담당하는 클래스
 */
@Component
@RequiredArgsConstructor
public class ExamSubmissionValidator {

	private final ExamBaseValidator examBaseValidator;

	private final ExamResultRepository examResultRepository;

	// 시험 제출 가능 여부 검증  (이미 제출된 시험인지 확인)
	public void validateSubmittable(Long examId, Long studentId) {
		examBaseValidator.validateExamExists(examId);

		ExamResult result = examResultRepository
				.findByExamIdAndStudentId(examId, studentId)
				.orElseThrow(() -> new BusinessBaseException(ErrorCode.EXAM_RESULT_NOT_FOUND));

		if (result.getStatus() == ExamStatus.SUBMITTED) {
			throw new BusinessBaseException(ErrorCode.EXAM_ALREADY_SUBMITTED);
		}
	}

	// 모든 문제에 답변 여부 검증
	public void validateAllQuestionsAnswered(ExamResult examResult) {
		int totalQuestions = examResult.getExam().getQuestions().size();
		int answeredQuestions = examResult.getScores().size();

		if (answeredQuestions < totalQuestions) {
			throw new BusinessBaseException(ErrorCode.EXAM_NOT_ALL_QUESTIONS_ANSWERED);
		}
	}

}
