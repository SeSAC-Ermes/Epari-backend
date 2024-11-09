package com.example.epari.course.domain;

import com.example.epari.global.common.base.BaseTimeEntity;
import com.example.epari.user.domain.Student;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 강의-학생 관계를 관리하는 연결 엔티티
 */
@Entity
@Table(name = "course_students")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class CourseStudent extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "course_id")
	private Course course;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "student_id")
	private Student student;

	public CourseStudent(Course course, Student student) {
		this.course = course;
		this.student = student;
	}

}
