package com.example.epari.course.domain;

import java.time.LocalDate;

import com.example.epari.global.common.base.BaseTimeEntity;
import com.example.epari.global.common.enums.AttendanceStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 출석 정보를 관리하는 엔티티
 */
@Entity
@Table(name = "attendances")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Attendance extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	private CourseStudent courseStudent;

	@Column(nullable = false)
	private LocalDate date; // 출석일

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AttendanceStatus status;

	@Builder
	private Attendance(CourseStudent courseStudent, LocalDate date) {
		this.courseStudent = courseStudent;
		this.date = date;
		this.status = AttendanceStatus.ABSENT;
	}

	public void updateStatus(AttendanceStatus status) {
		this.status = status;
	}

}
