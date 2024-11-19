package com.example.epari.global.validator;

import org.springframework.stereotype.Component;

import com.example.epari.course.repository.CourseRepository;
import com.example.epari.exam.domain.Exam;
import com.example.epari.exam.domain.ExamQuestion;
import com.example.epari.exam.repository.ExamQuestionRepository;
import com.example.epari.exam.repository.ExamRepository;
import com.example.epari.global.exception.BusinessBaseException;
import com.example.epari.global.exception.ErrorCode;
import com.example.epari.exam.repository.ExamResultRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ExamQuestionValidator {
    private final CourseRepository courseRepository;
    private final ExamRepository examRepository;
    private final ExamQuestionRepository examQuestionRepository;
	private final ExamResultRepository examResultRepository;

    public void validateInstructorAccess(Long courseId, String instructorEmail) {
        if (!courseRepository.existsByCourseIdAndInstructorEmail(courseId, instructorEmail)) {
            throw new BusinessBaseException(ErrorCode.UNAUTHORIZED_EXAM_ACCESS);
        }
    }

    public Exam validateExamAccess(Long courseId, Long examId) {
        return examRepository.findByCourseIdAndId(courseId, examId)
                .orElseThrow(() -> new BusinessBaseException(ErrorCode.EXAM_NOT_FOUND));
    }

    public ExamQuestion validateQuestionAccess(Long examId, Long questionId) {
        return examQuestionRepository.findByExamIdAndId(examId, questionId)
                .orElseThrow(() -> new BusinessBaseException(ErrorCode.EXAM_QUESTION_NOT_FOUND));
    }

	public void validateExamTime(Exam exam) {
        if (exam.isBeforeExam()) {
            throw new BusinessBaseException(ErrorCode.EXAM_NOT_STARTED);
        }
        if (exam.isAfterExam()) {
            throw new BusinessBaseException(ErrorCode.EXAM_ALREADY_ENDED);
        }
    }

    public void validateNotAlreadyStarted(Long examId, String studentEmail) {
        examResultRepository.findByExamIdAndStudentEmail(examId, studentEmail)
                .ifPresent(result -> {
                    throw new BusinessBaseException(ErrorCode.EXAM_ALREADY_STARTED);
                });
    }
}
