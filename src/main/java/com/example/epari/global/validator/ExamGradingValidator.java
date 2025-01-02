package com.example.epari.global.validator;

import org.springframework.stereotype.Component;

import com.example.epari.exam.domain.ExamResult;
import com.example.epari.exam.domain.ExamScore;
import com.example.epari.exam.repository.ExamResultRepository;
import com.example.epari.global.common.enums.ExamStatus;
import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * 시험 채점 관련 검증을 담당하는 클래스
 */
@Component
@RequiredArgsConstructor
public class ExamGradingValidator {

	private final ExamResultRepository examResultRepository;

	private final CourseAccessValidator courseAccessValidator;

	private final ExamBaseValidator examBaseValidator;

	// 채점 권한 검증
	public void validateGradingAccess(Long courseId, Long examId, Long examResultId, Long instructorId) {
		// 강사 권한 검증
		courseAccessValidator.validateInstructorAccess(courseId, instructorId);

		// 시험 존재 여부 및 과목 일치 여부 검증
		examBaseValidator.validateExamCourse(courseId, examId);
	}

	// 채점을 위한 시험 결과 검증
	public ExamResult validateExamResultForGrading(Long examResultId) {
		ExamResult examResult = examResultRepository.findById(examResultId)
				.orElseThrow(() -> new BusinessBaseException(ErrorCode.EXAM_RESULT_NOT_FOUND));

		// 제출된 상태인지 확인
		if (examResult.getStatus() != ExamStatus.SUBMITTED) {
			throw new BusinessBaseException(ErrorCode.EXAM_NOT_SUBMITTED);
		}

		return examResult;
	}

	// 채점 수정을 위한 시험 결과 검증
	public ExamResult validateExamResultForGradingUpdate(Long examResultId) {
		ExamResult examResult = examResultRepository.findById(examResultId)
				.orElseThrow(() -> new BusinessBaseException(ErrorCode.EXAM_RESULT_NOT_FOUND));

		// 채점된 상태인지 확인
		if (examResult.getStatus() != ExamStatus.GRADED) {
			throw new BusinessBaseException(ErrorCode.EXAM_NOT_SUBMITTED);
		}

		return examResult;
	}

	// 시험 결과의 총점 정합성 검증
	public boolean verifyTotalScore(ExamResult examResult) {
		int calculatedTotal = examResult.getScores().stream()
				.mapToInt(ExamScore::getEarnedScore)
				.sum();
		return calculatedTotal == examResult.getEarnedScore();
	}

}
