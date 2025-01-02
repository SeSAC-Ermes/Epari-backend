package com.example.epari.exam.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.epari.exam.domain.Choice;
import com.example.epari.exam.domain.Exam;
import com.example.epari.exam.domain.ExamQuestion;
import com.example.epari.exam.domain.MultipleChoiceQuestion;
import com.example.epari.exam.domain.SubjectiveQuestion;
import com.example.epari.exam.dto.request.CreateQuestionRequestDto;
import com.example.epari.exam.dto.request.UpdateQuestionRequestDto;
import com.example.epari.exam.dto.response.ExamQuestionResponseDto;
import com.example.epari.exam.repository.ExamQuestionRepository;
import com.example.epari.global.common.enums.ExamQuestionType;
import com.example.epari.global.common.enums.ExamStatus;
import com.example.epari.global.validator.ExamQuestionValidator;
import com.example.epari.global.validator.ExamStatusValidator;
import com.example.epari.user.domain.Instructor;
import com.example.epari.global.validator.CourseAccessValidator;

import lombok.RequiredArgsConstructor;

/**
 * 시험 문제 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ExamQuestionService {

	private final ExamQuestionValidator examQuestionValidator;

	private final ExamStatusValidator examStatusValidator;

	private final ExamQuestionRepository examQuestionRepository;

	private final CourseAccessValidator courseAccessValidator;

	// 문제 생성
	public Long addQuestion(Long courseId, Long examId, CreateQuestionRequestDto dto, String instructorEmail) {
		Instructor instructor = courseAccessValidator.validateInstructorEmail(instructorEmail);
		examQuestionValidator.validateInstructorAccess(courseId, instructor.getId());
		Exam exam = examQuestionValidator.validateExamAccess(courseId, examId);
		examStatusValidator.validateExamStatus(examId, ExamStatus.SCHEDULED);

		ExamQuestion question = dto.toEntity(exam);
		exam.addQuestion(question);
		return question.getId();
	}

	// 문제 순서 변경
	public void reorderQuestions(Long courseId, Long examId, Long questionId, int newNumber,
			String instructorEmail) {
		Instructor instructor = courseAccessValidator.validateInstructorEmail(instructorEmail);
		examQuestionValidator.validateInstructorAccess(courseId, instructor.getId());
		Exam exam = examQuestionValidator.validateExamAccess(courseId, examId);
		ExamQuestion question = examQuestionValidator.validateQuestionAccess(examId, questionId);

		examStatusValidator.validateExamStatus(examId, ExamStatus.SCHEDULED);

		exam.reorderQuestions(questionId, newNumber);
	}

	// 문제 수정
	public ExamQuestionResponseDto updateQuestion(Long courseId, Long examId, Long questionId,
			UpdateQuestionRequestDto requestDto, String instructorEmail) {
		Instructor instructor = courseAccessValidator.validateInstructorEmail(instructorEmail);
		examQuestionValidator.validateInstructorAccess(courseId, instructor.getId());
		examQuestionValidator.validateExamAccess(courseId, examId);
		ExamQuestion question = examQuestionValidator.validateQuestionAccess(examId, questionId);

		// Validator로 이동
		examQuestionValidator.validateQuestionTypeUnchanged(question, requestDto.getType());

		// 문제 유형에 따른 수정 처리
		if (requestDto.getType() == ExamQuestionType.MULTIPLE_CHOICE) {
			return updateMultipleChoiceQuestion((MultipleChoiceQuestion)question, requestDto);
		} else {
			return updateSubjectiveQuestion((SubjectiveQuestion)question, requestDto);
		}
	}

	// 객관식 문제 수정
	private ExamQuestionResponseDto updateMultipleChoiceQuestion(MultipleChoiceQuestion question,
			UpdateQuestionRequestDto requestDto) {
		// 기존 선택지 모두 삭제
		question.getChoices().clear();

		// 새로운 선택지 추가
		requestDto.getChoices().forEach(choiceDto -> {
			Choice choice = Choice.builder()
					.number(choiceDto.getNumber())
					.choiceText(choiceDto.getChoiceText())
					.build();
			question.addChoice(choice);
		});

		// 문제 정보 업데이트
		updateQuestionCommonFields(question, requestDto);

		return ExamQuestionResponseDto.fromQuestionWithAnswer(question);
	}

	// 주관식 문제 수정
	private ExamQuestionResponseDto updateSubjectiveQuestion(SubjectiveQuestion question,
			UpdateQuestionRequestDto requestDto) {
		// 문제 정보 업데이트
		updateQuestionCommonFields(question, requestDto);
		return ExamQuestionResponseDto.fromQuestionWithAnswer(question);
	}

	// 문제 공통 사항 수정
	private void updateQuestionCommonFields(ExamQuestion question, UpdateQuestionRequestDto requestDto) {
		question.updateQuestion(
				requestDto.getQuestionText(),
				requestDto.getScore(),
				requestDto.getCorrectAnswer()
		);
	}

	// 문제 삭제
	public void deleteQuestion(Long courseId, Long examId, Long questionId, String instructorEmail) {
		Instructor instructor = courseAccessValidator.validateInstructorEmail(instructorEmail);
		examQuestionValidator.validateInstructorAccess(courseId, instructor.getId());
		Exam exam = examQuestionValidator.validateExamAccess(courseId, examId);
		ExamQuestion question = examQuestionValidator.validateQuestionAccess(examId, questionId);

		examStatusValidator.validateExamStatus(examId, ExamStatus.SCHEDULED);
		examQuestionValidator.validateQuestionDeletable(questionId);

		// 삭제할 문제의 번호 저장
		int deletedQuestionNumber = question.getExamNumber();

		// exam의 questions 리스트에서 제거
		exam.getQuestions().remove(question);

		// 문제 삭제
		examQuestionRepository.delete(question);

		// 남은 문제들의 번호 재정렬
		exam.getQuestions().stream()
				.filter(q -> q.getExamNumber() > deletedQuestionNumber)
				.forEach(q -> q.updateExamNumber(q.getExamNumber() - 1));
	}

}
