package com.example.epari.exam.dto.response;

import java.time.LocalDateTime;

import com.example.epari.exam.domain.Exam;
import com.example.epari.exam.domain.ExamResult;
import com.example.epari.exam.dto.common.ExamStatistics;
import com.example.epari.global.common.enums.ExamStatus;
import com.example.epari.user.domain.Instructor;

import lombok.Builder;
import lombok.Getter;

/**
 * 시험 정보를 반환하는 Dto
 */
@Getter
@Builder
public class ExamSummaryDto {

	private Long id;

	private String title;

	private LocalDateTime examDateTime;

	private Integer duration;

	private Integer totalScore;

	private LocalDateTime createdAt;  // 출제일자 추가

	private InstructorInfo instructor;

	// 강사용 추가 정보
	private Integer totalStudentCount;      // 총 수강생 수

	private Integer submittedStudentCount;  // 제출한 학생 수

	private Double averageScore;            // 평균 점수

	// 학생용 추가 정보
	private ExamStatus status;              // 학생별 시험 상태

	private Integer earnedScore;            // 취득 점수

	private LocalDateTime submittedAt;      // 제출 시간

	@Getter
	@Builder
	public static class InstructorInfo {

		private Long id;

		private String name;

		private String email;

		public static InstructorInfo from(Instructor instructor) {
			return InstructorInfo.builder()
					.id(instructor.getId())
					.name(instructor.getName())
					.email(instructor.getEmail())
					.build();
		}

	}

	// 강사용 DTO 생성
	public static ExamSummaryDto forInstructor(Exam exam, ExamStatistics statistics) {
		return ExamSummaryDto.builder()
				.id(exam.getId())
				.title(exam.getTitle())
				.examDateTime(exam.getExamDateTime())
				.duration(exam.getDuration())
				.totalScore(exam.getTotalScore())
				.createdAt(exam.getCreatedAt())  // 출제일자 추가
				.instructor(InstructorInfo.from(exam.getCourse().getInstructor())) // 작성자 정보 추가
				.totalStudentCount(statistics.getTotalStudentCount())
				.submittedStudentCount(statistics.getSubmittedStudentCount())
				.averageScore(statistics.getAverageScore())
				.build();
	}

	// 학생용 DTO 생성
	public static ExamSummaryDto forStudent(Exam exam, ExamResult result) {
		return ExamSummaryDto.builder()
				.id(exam.getId())
				.title(exam.getTitle())
				.examDateTime(exam.getExamDateTime())
				.duration(exam.getDuration())
				.totalScore(exam.getTotalScore())
				.createdAt(exam.getCreatedAt())  // 출제일자 추가
				.instructor(InstructorInfo.from(exam.getCourse().getInstructor()))  // 작성자 정보 추가
				.status(result != null ? result.getStatus() : ExamStatus.NOT_SUBMITTED)
				.earnedScore(result != null ? result.getEarnedScore() : null)
				.submittedAt(result != null ? result.getSubmitTime() : null)
				.build();
	}

	public static ExamSummaryDto forNewExam(Exam exam) {
		return ExamSummaryDto.builder()
				.id(exam.getId())
				.title(exam.getTitle())
				.examDateTime(exam.getExamDateTime())
				.duration(exam.getDuration())
				.createdAt(exam.getCreatedAt())  // 출제일자 추가
				.instructor(InstructorInfo.from(exam.getCourse().getInstructor()))  // 작성자 정보 추가
				.totalScore(exam.getTotalScore())
				.status(ExamStatus.SCHEDULED)
				.build();
	}

}
