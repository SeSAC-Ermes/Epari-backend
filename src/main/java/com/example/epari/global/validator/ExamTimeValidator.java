package com.example.epari.global.validator;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.example.epari.exam.domain.Exam;
import com.example.epari.exam.repository.ExamRepository;
import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * 시험 시간 관련 검증을 담당하는 클래스
 */
@Component
@RequiredArgsConstructor
public class ExamTimeValidator {

	private final ExamRepository examRepository;

	// 시험 기간이 유효한지 검증 (시작 전인지))
	public void validateExamPeriod(Long examId) {
		Exam exam = examRepository.findById(examId)
				.orElseThrow(() -> new BusinessBaseException(ErrorCode.EXAM_NOT_FOUND));

		if (!exam.isBeforeExam()) {
			throw new BusinessBaseException(ErrorCode.EXAM_ALREADY_STARTED);
		}
	}

	// 시험 응시 가능 시간인지 검증
	public void validateExamTime(Long examId) {
		Exam exam = examRepository.findById(examId)
				.orElseThrow(() -> new BusinessBaseException(ErrorCode.EXAM_NOT_FOUND));

		if (!exam.isDuringExam()) {
			throw new BusinessBaseException(ErrorCode.EXAM_NOT_IN_PROGRESS);
		}
	}

	// 남은 시험 시간 검증
	public void validateExamTimeRemaining(Exam exam) {
		if (!exam.isDuringExam()) {
			throw new BusinessBaseException(ErrorCode.EXAM_TIME_EXPIRED);
		}
	}

	// 시험 제출 가능 시간인지 검증 (종료 시간 이전인지)
	public void validateSubmissionTime(Long examId) {
		Exam exam = examRepository.findById(examId)
				.orElseThrow(() -> new BusinessBaseException(ErrorCode.EXAM_NOT_FOUND));

		if (LocalDateTime.now().isAfter(exam.getEndDateTime())) {
			throw new BusinessBaseException(ErrorCode.EXAM_ALREADY_ENDED);
		}
	}

}
