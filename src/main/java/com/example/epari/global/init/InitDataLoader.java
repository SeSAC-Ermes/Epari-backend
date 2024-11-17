package com.example.epari.global.init;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.epari.course.domain.Course;
import com.example.epari.course.domain.CourseStudent;
import com.example.epari.course.domain.Curriculum;
import com.example.epari.course.repository.CourseRepository;
import com.example.epari.course.repository.CourseStudentRepository;
import com.example.epari.course.repository.CurriculumRepository;
import com.example.epari.user.domain.Instructor;
import com.example.epari.user.domain.Student;
import com.example.epari.user.repository.InstructorRepository;
import com.example.epari.user.repository.StudentRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Profile("dev")
public class InitDataLoader implements ApplicationRunner {

	private final InstructorRepository instructorRepository;

	private final StudentRepository studentRepository;

	private final CourseRepository courseRepository;

	private final CurriculumRepository curriculumRepository;

	private final CourseStudentRepository courseStudentRepository;

	@Transactional
	@Override
	public void run(ApplicationArguments args) {
		// 1. 강사 생성
		List<Instructor> instructors = createInstructors();

		// 2. 학생 20명 생성
		List<Student> students = createStudents();

		// 3. 강의 생성
		List<Course> courses = createCourses(instructors);

		// 4. 각 강의별 커리큘럼 생성
		for (Course course : courses) {
			createCurriculums(course);
		}

		// 5. 수강 신청 데이터 생성
		// 첫 번째 강의: 모든 학생 수강
		List<CourseStudent> courseStudents1 = createCourseStudents(courses.get(0), students);
		// 두 번째 강의: 절반의 학생만 수강
		List<CourseStudent> courseStudents2 = createCourseStudents(courses.get(1),
				students.subList(0, students.size() / 2));

	}

	// 강사 추가를 위해 list로 반환
	private List<Instructor> createInstructors() {
		List<Instructor> instructors = new ArrayList<>();
		Instructor instructor1 = Instructor.createInstructor(
				"instructor@test.com",
				"1234",
				"윤강사",
				"010-1234-5678"
		);

		Instructor instructor2 = Instructor.createInstructor(
				"instructor2@test.com",
				"1234",
				"김강사",
				"010-9876-5432"
		);

		Instructor instructor3 = Instructor.createInstructor(
				"instructor3@test.com",
				"1234",
				"이강사",
				"010-7777-4444"
		);

		instructors.add(instructor1);
		instructors.add(instructor2);
		instructors.add(instructor3);

		return instructorRepository.saveAll(instructors);
	}

	private List<Student> createStudents() {
		List<Student> students = new ArrayList<>();
		for (int i = 1; i <= 20; i++) {
			Student student = Student.createStudent(
					"student" + i + "@test.com",
					"1234",
					"학생" + i,
					"010-1234-" + String.format("%04d", i)
			);
			students.add(student);
		}
		return studentRepository.saveAll(students);
	}

	private List<Course> createCourses(List<Instructor> instructors) {
		List<Course> courses = new ArrayList<>();

		// 첫 번째 강의 (기존 강의)
		Course course1 = Course.builder()
				.name("AWS 클라우드를 활용한 MSA 기반 자바 개발자 양성 과정")
				.instructor(instructors.get(0))
				.startDate(LocalDate.of(2024, 7, 3))
				.endDate(LocalDate.of(2025, 1, 7))
				.classroom("교육장 301호")
				.build();

		course1.updateCourseImage("https://epari-dev.s3.ap-northeast-2.amazonaws.com/course-images/AWS_클라우드.jpg");

		// 두 번째 강의 (새로운 강의)
		Course course2 = Course.builder()
				.name("스프링 부트와 리액트를 활용한 풀스택 개발자 과정")
				.instructor(instructors.get(1))
				.startDate(LocalDate.of(2024, 8, 5))
				.endDate(LocalDate.of(2025, 2, 28))
				.classroom("교육장 302호")
				.build();

		course2.updateCourseImage("https://epari-dev.s3.ap-northeast-2.amazonaws.com/course-images/스프링부트_리액트.jpg");

		courses.add(course1);
		courses.add(course2);

		return courseRepository.saveAll(courses);
	}

	private void createCurriculums(Course course) {
		List<Curriculum> curriculums = new ArrayList<>();
		Map<LocalDate, CurriculumInfo> topicsByDate;

		if (course.getName().contains("AWS")) {
			topicsByDate = getAwsCourseCurriculum();
		} else {
			topicsByDate = getFullstackCourseCurriculum();
		}

		LocalDate currentDate = course.getStartDate();
		while (!currentDate.isAfter(course.getEndDate())) {
			if (topicsByDate.containsKey(currentDate)) {
				CurriculumInfo info = topicsByDate.get(currentDate);
				Curriculum curriculum = Curriculum.builder()
						.course(course)
						.date(currentDate)
						.topic(info.topic)
						.description(info.description)
						.build();
				curriculums.add(curriculum);
			}
			currentDate = currentDate.plusDays(1);
		}

		curriculumRepository.saveAll(curriculums);
	}

	private List<CourseStudent> createCourseStudents(Course course, List<Student> students) {
		List<CourseStudent> courseStudents = new ArrayList<>();
		for (Student student : students) {
			CourseStudent courseStudent = new CourseStudent(course, student);
			courseStudents.add(courseStudent);
		}
		return courseStudentRepository.saveAll(courseStudents);
	}

	private Map<LocalDate, CurriculumInfo> getAwsCourseCurriculum() {
		Map<LocalDate, CurriculumInfo> topics = new HashMap<>();

		// 1. 웹서비스 개발을 위한 프로그래밍 기본 다지기(JAVA & Database)
		// 2024-07-03 ~ 2024-07-23
		addTopicRange(topics,
				LocalDate.of(2024, 7, 3),
				LocalDate.of(2024, 7, 23),
				"웹서비스 개발을 위한 프로그래밍 기본 다지기(JAVA & Database)",
				LocalTime.of(10, 0),
				LocalTime.of(17, 0));

		// 2. Front-End 개발기술(ECMAScript & React)
		// 2024-07-24 ~ 2024-08-14
		addTopicRange(topics,
				LocalDate.of(2024, 7, 24),
				LocalDate.of(2024, 8, 14),
				"Front-End 개발기술(ECMAScript & React)",
				LocalTime.of(10, 0),
				LocalTime.of(17, 0));

		// 3. Spring Boot 프레임워크와 Spring JPA & MyBatis
		// 2024-08-19 ~ 2024-09-13
		addTopicRange(topics,
				LocalDate.of(2024, 8, 19),
				LocalDate.of(2024, 9, 13),
				"Spring Boot 프레임워크와 Spring JPA & MyBatis",
				LocalTime.of(10, 0),
				LocalTime.of(17, 0));

		// 4. 클라우드의 시작, Linux & Network
		// 2024-09-19 ~ 2024-09-25
		addTopicRange(topics,
				LocalDate.of(2024, 9, 19),
				LocalDate.of(2024, 9, 25),
				"클라우드의 시작, Linux & Network",
				LocalTime.of(10, 0),
				LocalTime.of(17, 0));

		// 5. 클라우드 MSA 기반 인프라 구현을 위한 Docker & Kubernetes 활용
		// 2024-09-26 ~ 2024-10-07
		addTopicRange(topics,
				LocalDate.of(2024, 9, 26),
				LocalDate.of(2024, 10, 7),
				"클라우드 MSA 기반 인프라 구현을 위한 Docker & Kubernetes 활용",
				LocalTime.of(10, 0),
				LocalTime.of(17, 0));

		// 6. 클라우드 핵심 인프라와 네트워크 및 서비스 설계와 구축
		// 2024-10-08 ~ 2024-10-17
		addTopicRange(topics,
				LocalDate.of(2024, 10, 8),
				LocalDate.of(2024, 10, 17),
				"클라우드 핵심 인프라와 네트워크 및 서비스 설계와 구축",
				LocalTime.of(10, 0),
				LocalTime.of(17, 0));

		// 7. 클라우드 MSA 기반 개발자 필수 기술
		// 2024-10-18 ~ 2024-10-29
		addTopicRange(topics,
				LocalDate.of(2024, 10, 18),
				LocalDate.of(2024, 10, 29),
				"클라우드 MSA 기반 개발자 필수 기술",
				LocalTime.of(10, 0),
				LocalTime.of(17, 0));

		// 8. 1차 프로젝트
		// 2024-10-30 ~ 2024-11-19
		addTopicRange(topics,
				LocalDate.of(2024, 10, 30),
				LocalDate.of(2024, 11, 19),
				"1차 프로젝트",
				LocalTime.of(10, 0),
				LocalTime.of(17, 0));

		// 9. MSA 기반 Spring Cloud
		// 2024-11-20 ~ 2024-11-26
		addTopicRange(topics,
				LocalDate.of(2024, 11, 20),
				LocalDate.of(2024, 11, 26),
				"MSA 기반 Spring Cloud",
				LocalTime.of(10, 0),
				LocalTime.of(17, 0));

		// 10. AWS SDK와 Lambda
		// 2024-11-27 ~ 2024-12-05
		addTopicRange(topics,
				LocalDate.of(2024, 11, 27),
				LocalDate.of(2024, 12, 5),
				"AWS SDK 와 Lambda",
				LocalTime.of(10, 0),
				LocalTime.of(17, 0));

		// 11. 2차 프로젝트
		// 2024-12-06 ~ 2025-01-07
		addTopicRange(topics,
				LocalDate.of(2024, 12, 6),
				LocalDate.of(2025, 1, 7),
				"2차 프로젝트",
				LocalTime.of(10, 0),
				LocalTime.of(17, 0));

		return topics;
	}

	private Map<LocalDate, CurriculumInfo> getFullstackCourseCurriculum() {
		Map<LocalDate, CurriculumInfo> topics = new HashMap<>();

		// 1. 자바스크립트 기초와 ES6+
		addTopicRange(topics,
				LocalDate.of(2024, 8, 5),
				LocalDate.of(2024, 8, 30),
				"자바스크립트 기초와 ES6+",
				LocalTime.of(10, 0),
				LocalTime.of(17, 0));

		// 2. 리액트 기초와 실전 프로젝트
		addTopicRange(topics,
				LocalDate.of(2024, 9, 2),
				LocalDate.of(2024, 9, 27),
				"리액트 기초와 실전 프로젝트",
				LocalTime.of(10, 0),
				LocalTime.of(17, 0));

		// 3. 스프링 부트와 JPA 심화
		addTopicRange(topics,
				LocalDate.of(2024, 9, 30),
				LocalDate.of(2024, 10, 25),
				"스프링 부트와 JPA 심화",
				LocalTime.of(10, 0),
				LocalTime.of(17, 0));

		// 4. REST API 설계와 구현
		addTopicRange(topics,
				LocalDate.of(2024, 10, 28),
				LocalDate.of(2024, 11, 22),
				"REST API 설계와 구현",
				LocalTime.of(10, 0),
				LocalTime.of(17, 0));

		// 5. 보안과 인증/인가 구현
		addTopicRange(topics,
				LocalDate.of(2024, 11, 25),
				LocalDate.of(2024, 12, 20),
				"보안과 인증/인가 구현",
				LocalTime.of(10, 0),
				LocalTime.of(17, 0));

		// 6. 팀 프로젝트 진행 및 포트폴리오 작성
		addTopicRange(topics,
				LocalDate.of(2024, 12, 23),
				LocalDate.of(2025, 2, 28),
				"팀 프로젝트 진행 및 포트폴리오 작성",
				LocalTime.of(10, 0),
				LocalTime.of(17, 0));

		return topics;
	}

	private void addTopicRange(
			Map<LocalDate, CurriculumInfo> topics,
			LocalDate startDate,
			LocalDate endDate,
			String topic,
			LocalTime startTime,
			LocalTime endTime
	) {
		LocalDate currentDate = startDate;
		while (!currentDate.isAfter(endDate)) {
			if (!isWeekend(currentDate)) {
				String description = String.format("%s 일자 강의에 대한 상세 설명입니다.",
						currentDate.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")));
				topics.put(currentDate, new CurriculumInfo(topic, description, startTime, endTime));
			}
			currentDate = currentDate.plusDays(1);
		}
	}

	private boolean isWeekend(LocalDate date) {
		return date.getDayOfWeek() == java.time.DayOfWeek.SATURDAY
			   || date.getDayOfWeek() == java.time.DayOfWeek.SUNDAY;
	}

	private static class CurriculumInfo {

		String topic;

		String description;

		LocalTime startTime;

		LocalTime endTime;

		CurriculumInfo(String topic, String description, LocalTime startTime, LocalTime endTime) {
			this.topic = topic;
			this.description = description;
			this.startTime = startTime;
			this.endTime = endTime;
		}

	}

}
