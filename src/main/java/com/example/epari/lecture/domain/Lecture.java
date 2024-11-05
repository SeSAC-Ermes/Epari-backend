package com.example.epari.lecture.domain;

import java.time.LocalDate;

import com.example.epari.global.common.base.BaseTimeEntity;
import com.example.epari.user.domain.Instructor;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 강의 정보를 관리하는 엔티티
 * - 강의 기본 정보(이름, 기간, 강의실 등) 관리
 */
@Entity
@Table(name = "lectures")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Lecture extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	private LocalDate startDate;

	private LocalDate endDate;

	private String classroom;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "instructor_id")
	private Instructor instructor;

	@Builder
	private Lecture(String name, LocalDate startDate, LocalDate endDate, String classroom, Instructor instructor) {
		this.name = name;
		this.startDate = startDate;
		this.endDate = endDate;
		this.classroom = classroom;
		this.instructor = instructor;
	}

	//강의 생성 메서드(강사 배정)
	public static Lecture createLecture(String name, LocalDate startDate, LocalDate endDate,
			String classroom, Instructor instructor) {
		return Lecture.builder()
				.name(name)
				.startDate(startDate)
				.endDate(endDate)
				.classroom(classroom)
				.instructor(instructor)
				.build();
	}

	// 강의 수정 메서드
	public void updateLecture(String name, LocalDate startDate, LocalDate endDate, String classroom) {
		this.name = name;
		this.startDate = startDate;
		this.endDate = endDate;
		this.classroom = classroom;
	}

}
