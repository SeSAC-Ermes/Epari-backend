package com.example.epari.exam.dto.response;

import java.util.List;
import java.util.stream.Collectors;

import com.example.epari.exam.domain.ExamQuestion;
import com.example.epari.exam.domain.MultipleChoiceQuestion;
import com.example.epari.exam.domain.SubjectiveQuestion;
import com.example.epari.global.common.enums.ExamQuestionType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExamQuestionResponseDto {

	private Long id;

	private String questionText;

	private int examNumber;

	private int score;

	private ExamQuestionType type;

	private String correctAnswer;

	private List<ChoiceResponseDto> choices;

	private QuestionImageResponseDto image;

	// 강사용 (정답 포함)
	public static ExamQuestionResponseDto fromQuestionWithAnswer(ExamQuestion question) {
		if (question instanceof MultipleChoiceQuestion) {
			return fromMultipleChoiceQuestion((MultipleChoiceQuestion)question, true);
		}
		return fromSubjectiveQuestion((SubjectiveQuestion)question, true);
	}

	// 학생용 (정답 제외)
	public static ExamQuestionResponseDto fromQuestionWithoutAnswer(ExamQuestion question) {
		if (question instanceof MultipleChoiceQuestion) {
			return fromMultipleChoiceQuestion((MultipleChoiceQuestion)question, false);
		}
		return fromSubjectiveQuestion((SubjectiveQuestion)question, false);
	}

	private static ExamQuestionResponseDto fromMultipleChoiceQuestion(
			MultipleChoiceQuestion question, boolean includeAnswer) {
		return ExamQuestionResponseDto.builder()
				.id(question.getId())
				.questionText(question.getQuestionText())
				.examNumber(question.getExamNumber())
				.score(question.getScore())
				.type(ExamQuestionType.MULTIPLE_CHOICE)
				.correctAnswer(includeAnswer ? question.getCorrectAnswer() : null)
				.choices(question.getChoices().stream()
						.map(ChoiceResponseDto::from)
						.collect(Collectors.toList()))
				.image(question.getImage() != null ?
						QuestionImageResponseDto.from(question.getImage()) : null)
				.build();
	}

	private static ExamQuestionResponseDto fromSubjectiveQuestion(
			SubjectiveQuestion question, boolean includeAnswer) {
		return ExamQuestionResponseDto.builder()
				.id(question.getId())
				.questionText(question.getQuestionText())
				.examNumber(question.getExamNumber())
				.score(question.getScore())
				.type(ExamQuestionType.SUBJECTIVE)
				.correctAnswer(includeAnswer ? question.getCorrectAnswer() : null)
				.image(question.getImage() != null ?
						QuestionImageResponseDto.from(question.getImage()) : null)
				.build();
	}

}
