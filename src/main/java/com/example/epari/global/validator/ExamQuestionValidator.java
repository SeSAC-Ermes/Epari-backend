package com.example.epari.global.validator;

import org.springframework.stereotype.Component;

import com.example.epari.exam.domain.Exam;
import com.example.epari.exam.domain.ExamQuestion;
import com.example.epari.exam.repository.ExamQuestionRepository;
import com.example.epari.global.common.enums.ExamQuestionType;
import com.example.epari.global.common.enums.ExamStatus;
import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * 시험 문제 관련 검증을 담당하는 클래스
 */
@Component
@RequiredArgsConstructor
public class ExamQuestionValidator {

	private final ExamQuestionRepository examQuestionRepository;

	private final ExamBaseValidator examBaseValidator;

	private final ExamStatusValidator examStatusValidator;

	private final CourseAccessValidator courseAccessValidator;

	// 강사 접근 권한 검증
	public void validateInstructorAccess(Long courseId, Long instructorId) {
		courseAccessValidator.validateInstructorAccess(courseId, instructorId);
	}

	// 시험 접근 권한 검증
	public Exam validateExamAccess(Long courseId, Long examId) {
		examBaseValidator.validateExamCourse(courseId, examId);
		return examBaseValidator.validateExamExists(examId);
	}

	// 시험 문제 접근 권한 검증
	public ExamQuestion validateQuestionAccess(Long examId, Long questionId) {
		ExamQuestion question = validateQuestionExists(questionId);
		if (!question.getExam().getId().equals(examId)) {
			throw new BusinessBaseException(ErrorCode.UNAUTHORIZED_EXAM_ACCESS);
		}
		return question;
	}

	// 시험 문제 존재 여부 검증
	public ExamQuestion validateQuestionExists(Long questionId) {
		return examQuestionRepository.findById(questionId)
				.orElseThrow(() -> new BusinessBaseException(ErrorCode.EXAM_QUESTION_NOT_FOUND));
	}

	// 시험 문제 유형 변경 불가 검증
	public void validateQuestionTypeUnchanged(ExamQuestion question, ExamQuestionType newType) {
		if (!question.getType().equals(newType)) {
			throw new BusinessBaseException(ErrorCode.EXAM_QUESTION_TYPE_CHANGE_NOT_ALLOWED);
		}
	}

	// 시험 문제 삭제 가능 여부 검증
	public void validateQuestionDeletable(Long questionId) {
		ExamQuestion question = validateQuestionExists(questionId);
		examStatusValidator.validateExamStatus(question.getExam().getId(), ExamStatus.SCHEDULED);

		if (examQuestionRepository.hasSubmissions(questionId)) {
			throw new BusinessBaseException(ErrorCode.EXAM_QUESTION_HAS_SUBMISSIONS);
		}
	}

}
