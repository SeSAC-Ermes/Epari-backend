package com.example.epari.exam.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.example.epari.course.domain.Course;
import com.example.epari.exam.domain.Exam;
import com.example.epari.exam.domain.ExamResult;
import com.example.epari.global.common.enums.ExamStatus;

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

	private Long courseId;

	private List<ExamQuestionResponseDto> questions;

	private ExamStatus status;

	private LocalDateTime submitTime;

	private Integer earnedScore;

	private InstructorInfo instructor;

	@Getter
	@Builder
	public static class InstructorInfo {
		private Long id;
		private String name;
		private String email;

		public static InstructorInfo from(Course course) {
			return InstructorInfo.builder()
					.id(course.getInstructor().getId())
					.name(course.getInstructor().getName())
					.email(course.getInstructor().getEmail())
					.build();
		}
	}

	public static ExamResponseDto fromExamForInstructor(Exam exam) {
		return ExamResponseDto.builder()
				.id(exam.getId())
				.title(exam.getTitle())
				.examDateTime(exam.getExamDateTime())
				.duration(exam.getDuration())
				.totalScore(exam.getTotalScore())
				.description(exam.getDescription())
				.courseId(exam.getCourse().getId())
				.questions(exam.getQuestions().stream()
						.map(ExamQuestionResponseDto::fromQuestionWithAnswer)
						.collect(Collectors.toList()))
				.instructor(InstructorInfo.from(exam.getCourse()))
				.build();
	}

	public static ExamResponseDto fromExamForStudent(Exam exam, ExamResult result) {
		boolean canViewQuestions = !exam.isBeforeExam();
		return ExamResponseDto.builder()
				.id(exam.getId())
				.title(exam.getTitle())
				.examDateTime(exam.getExamDateTime())
				.duration(exam.getDuration())
				.totalScore(exam.getTotalScore())
				.description(exam.getDescription())
				.courseId(exam.getCourse().getId())
				.questions(canViewQuestions ? exam.getQuestions().stream()
						.map(ExamQuestionResponseDto::fromQuestionWithoutAnswer)
						.collect(Collectors.toList()) : null)
				.status(result != null ? result.getStatus() : null)
				.submitTime(result != null ? result.getSubmitTime() : null)
				.earnedScore(result != null ? result.getEarnedScore() : null)
				.instructor(InstructorInfo.from(exam.getCourse()))
				.build();
	}

}
