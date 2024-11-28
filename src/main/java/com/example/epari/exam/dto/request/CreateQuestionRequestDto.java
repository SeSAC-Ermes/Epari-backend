package com.example.epari.exam.dto.request;

import java.util.List;

import com.example.epari.exam.domain.Choice;
import com.example.epari.exam.domain.Exam;
import com.example.epari.exam.domain.ExamQuestion;
import com.example.epari.exam.domain.MultipleChoiceQuestion;
import com.example.epari.exam.domain.SubjectiveQuestion;
import com.example.epari.global.common.enums.ExamQuestionType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 시험 문제 생성 요청을 위한 DTO 클래스
 */
@Getter
@NoArgsConstructor
public class CreateQuestionRequestDto {

	@NotBlank(message = "문제 내용은 필수입니다")
	private String questionText;

	@Min(value = 1, message = "배점은 1점 이상이어야 합니다")
	private int score;

	@NotNull(message = "문제 유형은 필수입니다")
	private ExamQuestionType type;

	// 객관식일 경우
	private List<ChoiceRequestDto> choices;

	@NotBlank(message = "정답은 필수입니다")
	private String correctAnswer;

	public ExamQuestion toEntity(Exam exam) {
		// exam 유효성 검사
		if (exam == null) {
			throw new IllegalArgumentException("시험 정보는 필수입니다");
		}

		// 문제 번호는 현재 시험의 문제 개수 + 1
		int examNumber = exam.getQuestions().size() + 1;

		if (ExamQuestionType.MULTIPLE_CHOICE.equals(type)) {
			// 객관식 문제 유효성 검사
			if (choices == null || choices.isEmpty()) {
				throw new IllegalArgumentException("객관식 문제는 보기가 필요합니다");
			}

			// 객관식 정답 번호 유효성 검사
			try {
				int answerNumber = Integer.parseInt(correctAnswer);
				if (answerNumber < 1 || answerNumber > choices.size()) {
					throw new IllegalArgumentException("올바르지 않은 정답 번호입니다");
				}
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("객관식 문제의 정답은 숫자여야 합니다");
			}

			MultipleChoiceQuestion question = MultipleChoiceQuestion.builder()
					.questionText(questionText)
					.examNumber(examNumber)
					.score(score)
					.exam(exam)
					.correctAnswer(correctAnswer)  // 문자열로 저장된 선택지 번호
					.build();

			// 선택지 추가
			choices.forEach(choiceDto -> {
				Choice choice = Choice.builder()
						.number(choiceDto.getNumber())
						.choiceText(choiceDto.getChoiceText())
						.build();
				question.addChoice(choice);
			});

			return question;

		} else if (ExamQuestionType.SUBJECTIVE.equals(type)) {
			// 주관식 문제의 경우 바로 생성
			return SubjectiveQuestion.builder()
					.questionText(questionText)
					.examNumber(examNumber)
					.score(score)
					.exam(exam)
					.correctAnswer(correctAnswer)
					.build();
		}

		throw new IllegalArgumentException("지원하지 않는 문제 유형입니다: " + type);
	}

}
