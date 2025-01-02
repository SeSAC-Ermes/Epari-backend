package com.example.epari.global.validator;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.example.epari.exam.domain.Exam;
import com.example.epari.exam.dto.response.ExamSummaryDto;
import com.example.epari.global.common.enums.ExamStatus;
import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * 시험 상태 관련 검증을 담당하는 클래스
 */
@Component
@RequiredArgsConstructor
public class ExamStatusValidator {

	private final ExamBaseValidator examBaseValidator;

	// 시험 상태 검증
	public void validateExamStatus(Long examId, ExamStatus expectedStatus) {
		Exam exam = examBaseValidator.validateExamExists(examId);
		if (exam.getStatus() != expectedStatus) {
			switch (expectedStatus) {
				case SCHEDULED -> throw new BusinessBaseException(ErrorCode.EXAM_ALREADY_STARTED);
				case IN_PROGRESS -> throw new BusinessBaseException(ErrorCode.EXAM_NOT_IN_PROGRESS);
				case SUBMITTED -> throw new BusinessBaseException(ErrorCode.EXAM_NOT_SUBMITTED);
				case GRADING -> throw new BusinessBaseException(ErrorCode.GRADING_IN_PROGRESS);
				case GRADED -> throw new BusinessBaseException(ErrorCode.EXAM_ALREADY_GRADED);
				case COMPLETED -> throw new BusinessBaseException(ErrorCode.EXAM_ALREADY_ENDED);
				default -> throw new BusinessBaseException(ErrorCode.BAD_REQUEST);
			}
		}
	}

	// 시험 상태 확인
	public boolean matchesStatus(Exam exam, ExamStatus status, LocalDateTime now) {
		return switch (status) {
			case SCHEDULED -> exam.isBeforeExam();
			case IN_PROGRESS -> exam.isDuringExam();
			case SUBMITTED, GRADING, GRADED, COMPLETED -> exam.isAfterExam();
			default -> false;
		};
	}

	public void categorizeExam(Exam exam, ExamSummaryDto summaryDto,
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

}
