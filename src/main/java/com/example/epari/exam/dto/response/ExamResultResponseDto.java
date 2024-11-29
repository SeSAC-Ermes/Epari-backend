package com.example.epari.exam.dto.response;

import java.util.List;

import com.example.epari.exam.domain.ExamResult;
import com.example.epari.user.domain.Student;

import lombok.Builder;
import lombok.Getter;

/**
 * 시험 결과 조회 응답 데이터를 전달하기 위한 DTO 클래스 구현
 */
@Getter
@Builder
public class ExamResultResponseDto {

	private StudentInfo student;

	private List<ExamInfo> examResults;

	private double averageScore;

	// 학생 정보 응답 DTO
	@Getter
	@Builder
	public static class StudentInfo {

		private Long id;

		private String name;

		private String email;

		public static StudentInfo from(Student student) {
			return StudentInfo.builder()
					.id(student.getId())
					.name(student.getName())
					.email(student.getEmail())
					.build();
		}

	}

	// 시험 정보 응답 DTO
	@Getter
	@Builder
	public static class ExamInfo {

		private Long examId;

		private String examTitle;

		private Integer earnedScore;

		public static ExamInfo from(ExamResult examResult, int earnedScore) {
			return ExamInfo.builder()
					.examId(examResult.getExam().getId())
					.examTitle(examResult.getExam().getTitle())
					.earnedScore(earnedScore)
					.build();
		}

	}

}
