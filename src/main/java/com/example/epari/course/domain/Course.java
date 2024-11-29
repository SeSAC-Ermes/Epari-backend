package com.example.epari.course.domain;

import java.time.LocalDate;
import java.util.List;

import com.example.epari.global.common.base.BaseTimeEntity;
import com.example.epari.user.domain.Instructor;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 강의 정보를 관리하는 Entity 클래스
 * 강의의 기본 정보인 이름, 시작/종료일, 강의실, 담당 강사 등을 관리합니다.
 */
@Entity
@Table(name = "courses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Course extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	private LocalDate startDate;

	private LocalDate endDate;

	private String classroom;

	private String imageUrl;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "instructor_id")
	private Instructor instructor;

	@OneToMany(mappedBy = "course")
	private List<CourseStudent> courseStudents;

	@Builder
	private Course(String name, LocalDate startDate, LocalDate endDate, String classroom, Instructor instructor) {
		this.name = name;
		this.startDate = startDate;
		this.endDate = endDate;
		this.classroom = classroom;
		this.instructor = instructor;
	}

	//강의 생성 메서드(강사 배정)
	public static Course createCourse(String name, LocalDate startDate, LocalDate endDate,
			String classroom, Instructor instructor) {
		return Course.builder()
				.name(name)
				.startDate(startDate)
				.endDate(endDate)
				.classroom(classroom)
				.instructor(instructor)
				.build();
	}

	// 강의 수정 메서드
	public void updateCourse(String name, LocalDate startDate, LocalDate endDate, String classroom) {
		this.name = name;
		this.startDate = startDate;
		this.endDate = endDate;
		this.classroom = classroom;
	}

	// 강의 수정 메서드
	public void updateCourse(String name, LocalDate startDate, LocalDate endDate, String classroom,
			Instructor instructor) {
		this.name = name;
		this.startDate = startDate;
		this.endDate = endDate;
		this.classroom = classroom;
		this.instructor = instructor;
	}

	public void updateCourseImage(String imageUrl) {
		this.imageUrl = imageUrl;
	}

}
