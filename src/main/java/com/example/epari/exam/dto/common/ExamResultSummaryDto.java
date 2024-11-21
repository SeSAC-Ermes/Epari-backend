package com.example.epari.exam.dto.common;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.example.epari.exam.domain.ExamResult;
import com.example.epari.global.common.enums.ExamStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamResultSummaryDto {

	private String studentName;

	private String studentEmail;

	private ExamStatus status;

	private Integer totalScore;

	private LocalDateTime submittedAt;

	private List<AnswerDto> answers;

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class AnswerDto {

		private int questionNumber;

		private String questionText;

		private String studentAnswer;

		private String correctAnswer;

		private int earnedScore;

	}

	public static ExamResultSummaryDto from(ExamResult result) {
		List<AnswerDto> answers = result.getScores()
				.stream()
				.map(score -> AnswerDto.builder()
						.questionNumber(score.getQuestion().getExamNumber())
						.questionText(score.getQuestion().getQuestionText())
						.studentAnswer(score.getStudentAnswer())
						.correctAnswer(score.getQuestion().getCorrectAnswer())
						.earnedScore(score.getEarnedScore())
						.build())
				.sorted(Comparator.comparingInt(AnswerDto::getQuestionNumber))
				.collect(Collectors.toList());

		return ExamResultSummaryDto.builder()
				.studentName(result.getStudent().getName())
				.studentEmail(result.getStudent().getEmail())
				.status(result.getStatus())
				.totalScore(result.getEarnedScore())
				.submittedAt(result.getSubmitTime())
				.answers(answers)
				.build();
	}

}
