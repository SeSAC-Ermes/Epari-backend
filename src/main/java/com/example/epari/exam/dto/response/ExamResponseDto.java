package com.example.epari.exam.dto.response;

import java.time.LocalDateTime;

import com.example.epari.exam.domain.Exam;

import lombok.Builder;
import lombok.Getter;

/**
 * 시험 정보 조회 응답을 위한 DTO 클래스
 */
@Getter
@Builder
public class ExamResponseDto {

	private Long id;

	private String title;

	private LocalDateTime examDateTime;

	private Integer duration;

	private Integer totalScore;

	private String description;

	private Long courserId;

	public static ExamResponseDto fromExam(Exam exam) {
		return ExamResponseDto.builder()
				.id(exam.getId())
				.title(exam.getTitle())
				.examDateTime(exam.getExamDateTime())
				.duration(exam.getDuration())
				.totalScore(exam.getTotalScore())
				.description(exam.getDescription())
				.courserId(exam.getCourse().getId())
				.build();
	}

}
