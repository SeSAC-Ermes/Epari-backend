package com.example.epari.global.validator;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.example.epari.exam.domain.Exam;
import com.example.epari.exam.domain.ExamResult;
import com.example.epari.exam.repository.ExamRepository;
import com.example.epari.exam.repository.ExamResultRepository;
import com.example.epari.global.common.enums.ExamStatus;
import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * 시험 관련 기본 검증을 담당하는 클래스
 */
@Component
@RequiredArgsConstructor
public class ExamBaseValidator {

	private final ExamRepository examRepository;

	private final ExamResultRepository examResultRepository;

	// 해당 강의의 시험인지 검증
	public void validateExamCourse(Long courseId, Long examId) {
		Exam exam = validateExamExists(examId);
		if (!exam.getCourse().getId().equals(courseId)) {
			throw new BusinessBaseException(ErrorCode.UNAUTHORIZED_EXAM_ACCESS);
		}
	}

	// 시험 생성 시간 검증
	public void validateExamDateTime(LocalDateTime examDateTime) {
		LocalDateTime now = LocalDateTime.now();
		if (examDateTime.isBefore(now)) {
			throw new BusinessBaseException(ErrorCode.EXAM_ALREADY_ENDED);
		}
	}

	// 시험 생성 여부 검증
	public Exam validateExamExists(Long examId) {
		return examRepository.findById(examId)
				.orElseThrow(() -> new BusinessBaseException(ErrorCode.EXAM_NOT_FOUND));
	}

	// 학생의 시험 접근 권한 검증
	public void validateExamAccess(Long examId, Long studentId) {
		Exam exam = validateExamExists(examId);

		Optional<ExamResult> existingResult = examResultRepository.findByExamIdAndStudentId(
				examId,
				studentId
		);

		if (existingResult.isPresent() && existingResult.get().getStatus() != ExamStatus.IN_PROGRESS) {
			throw new BusinessBaseException(ErrorCode.EXAM_ALREADY_SUBMITTED);
		}
	}

}
