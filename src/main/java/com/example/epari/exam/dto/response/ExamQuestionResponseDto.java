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

	private List<ChoiceResponseDto> choices;  // 객관식인 경우에만 사용

	public static ExamQuestionResponseDto from(ExamQuestion question) {
		if (question instanceof MultipleChoiceQuestion) {
			MultipleChoiceQuestion mcq = (MultipleChoiceQuestion)question;
			return ExamQuestionResponseDto.builder()
					.id(mcq.getId())
					.questionText(mcq.getQuestionText())
					.examNumber(mcq.getExamNumber())
					.score(mcq.getScore())
					.type(ExamQuestionType.MULTIPLE_CHOICE)
					.correctAnswer(mcq.getCorrectAnswer())
					.choices(mcq.getChoices().stream()
							.map(ChoiceResponseDto::from)
							.collect(Collectors.toList()))
					.build();
		} else if (question instanceof SubjectiveQuestion) {
			SubjectiveQuestion sq = (SubjectiveQuestion)question;
			return ExamQuestionResponseDto.builder()
					.id(sq.getId())
					.questionText(sq.getQuestionText())
					.examNumber(sq.getExamNumber())
					.score(sq.getScore())
					.type(ExamQuestionType.SUBJECTIVE)
					.correctAnswer(sq.getCorrectAnswer())
					.build();
		}

		throw new IllegalArgumentException("지원하지 않는 문제 유형입니다");
	}

}
