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
import com.example.epari.exam.repository.ExamRepository;
import com.example.epari.global.common.enums.ExamQuestionType;
import com.example.epari.global.validator.CourseAccessValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ExamQuestionService {

	private final ExamRepository examRepository;

	private final CourseAccessValidator courseAccessValidator;

	private final ExamFinder examFinder;

	private final ExamQuestionRepository examQuestionRepository;

	// 문제 생성
	public Long addQuestion(Long courseId, Long examId, CreateQuestionRequestDto dto, String instructorEmail) {

		courseAccessValidator.validateInstructorAccess(courseId, instructorEmail);

		Exam exam = examFinder.findExam(courseId, examId);
		ExamQuestion question = dto.toEntity(exam);
		exam.addQuestion(question);

		return question.getId();
	}

	// 문제 순서 변경
	public void reorderQuestions(Long courseId, Long examId, Long questionId, int newNumber,
			String instructorEmail) {
		courseAccessValidator.validateInstructorAccess(courseId, instructorEmail);

		Exam exam = examFinder.findExam(courseId, examId);
		exam.reorderQuestions(questionId, newNumber);
	}

	// 문제 수정
	public ExamQuestionResponseDto updateQuestion(Long courseId, Long examId, Long questionId,
			UpdateQuestionRequestDto requestDto, String instructorEmail) {

		// 접근 권한 검증
		courseAccessValidator.validateInstructorAccess(courseId, instructorEmail);

		// 시험 존재 여부 확인
		Exam exam = examRepository.findById(examId)
				.orElseThrow(() -> new IllegalArgumentException("시험을 찾을 수 없습니다."));

		// 문제 존재 여부 확인
		ExamQuestion question = examQuestionRepository.findById(questionId)
				.orElseThrow(() -> new IllegalArgumentException("문제를 찾을 수 없습니다."));

		// 문제가 해당 시험에 속하는지 확인
		if (!question.getExam().getId().equals(examId)) {
			throw new IllegalArgumentException("해당 시험에 속한 문제가 아닙니다.");
		}

		// 문제 유형이 변경되었는지 확인
		if (!question.getType().equals(requestDto.getType())) {
			throw new IllegalArgumentException("문제 유형은 변경할 수 없습니다.");
		}

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

		return ExamQuestionResponseDto.from(question);
	}

	// 주관식 문제 수정
	private ExamQuestionResponseDto updateSubjectiveQuestion(SubjectiveQuestion question,
			UpdateQuestionRequestDto requestDto) {
		// 문제 정보 업데이트
		updateQuestionCommonFields(question, requestDto);

		return ExamQuestionResponseDto.from(question);
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
		// 접근 권한 검증
		courseAccessValidator.validateInstructorAccess(courseId, instructorEmail);

		// 시험 존재 여부 확인
		Exam exam = examFinder.findExam(courseId, examId);

		// 문제 존재 여부 확인
		ExamQuestion question = examQuestionRepository.findById(questionId)
				.orElseThrow(() -> new IllegalArgumentException("문제를 찾을 수 없습니다."));

		// 문제가 해당 시험에 속하는지 확인
		if (!question.getExam().getId().equals(examId)) {
			throw new IllegalArgumentException("해당 시험에 속한 문제가 아닙니다.");
		}

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

	private void validateDeletable(ExamQuestion question) {
		// TODO: 답안 제출 여부 등 검증 로직 추가 예정
		// 현재는 임시로 true 반환
		return;
	}

}
