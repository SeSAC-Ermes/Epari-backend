package com.example.epari.assignment.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.epari.course.domain.Course;

import com.example.epari.global.common.base.BaseTimeEntity;
import com.example.epari.global.common.base.BaseUser;
import com.example.epari.user.domain.Instructor;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 과제 관리를 위한 엔티티
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Assignment extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String title;

	@Column(columnDefinition = "LONGTEXT")
	private String description;

	@Column
	private LocalDateTime deadline;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "course_id")
	private Course course;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "instructor_id")
	private Instructor instructor;

	@OneToMany(mappedBy = "assignment", cascade = CascadeType.REMOVE)
	private List<Submission> submissions = new ArrayList<>();

	@OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<AssignmentFile> files = new ArrayList<>();

	@Builder
	private Assignment(String title, String description, LocalDateTime deadline,
					   Course course, Instructor instructor) {
		this.title = title;
		this.description = description;
		this.deadline = deadline;
		this.course = course;
		this.instructor = instructor;
	}

	// 과제 생성
	public static Assignment createAssignment(String title, String description,
											  LocalDateTime deadline, Course course, Instructor instructor) {
		return Assignment.builder()
				.title(title)
				.description(description)
				.deadline(deadline)
				.course(course)
				.instructor(instructor)
				.build();
	}

	// 과제 정보 수정
	public void updateAssignment(String title, String description, LocalDateTime deadline) {
		this.title = title;
		this.description = description;
		this.deadline = deadline;
	}


	// 파일 추가
	public void addFile(AssignmentFile file) {
		this.files.add(file);
	}

	// 파일 제거
	public void removeFile(AssignmentFile file) {
		this.files.remove(file);
	}

}
