package com.example.epari.global.init;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.epari.assignment.domain.Assignment;
import com.example.epari.assignment.domain.Submission;
import com.example.epari.assignment.repository.AssignmentRepository;
import com.example.epari.assignment.repository.SubmissionRepository;
import com.example.epari.course.domain.Attendance;
import com.example.epari.course.domain.Course;
import com.example.epari.course.domain.CourseContent;
import com.example.epari.course.domain.CourseStudent;
import com.example.epari.course.domain.Curriculum;
import com.example.epari.course.repository.AttendanceRepository;
import com.example.epari.course.repository.CourseContentRepository;
import com.example.epari.course.repository.CourseRepository;
import com.example.epari.course.repository.CourseStudentRepository;
import com.example.epari.course.repository.CurriculumRepository;
import com.example.epari.exam.domain.Choice;
import com.example.epari.exam.domain.Exam;
import com.example.epari.exam.domain.ExamQuestion;
import com.example.epari.exam.domain.ExamResult;
import com.example.epari.exam.domain.ExamScore;
import com.example.epari.exam.domain.MultipleChoiceQuestion;
import com.example.epari.exam.domain.SubjectiveQuestion;
import com.example.epari.exam.repository.ExamQuestionRepository;
import com.example.epari.exam.repository.ExamRepository;
import com.example.epari.exam.repository.ExamResultRepository;
import com.example.epari.global.common.enums.AttendanceStatus;
import com.example.epari.global.common.enums.SubmissionGrade;
import com.example.epari.global.common.enums.SubmissionStatus;
import com.example.epari.user.domain.Instructor;
import com.example.epari.user.domain.Student;
import com.example.epari.user.repository.InstructorRepository;
import com.example.epari.user.repository.StudentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Profile("dev")
@Slf4j
public class InitDataLoader implements ApplicationRunner {

	private final InstructorRepository instructorRepository;

	private final StudentRepository studentRepository;

	private final CourseRepository courseRepository;

	private final CurriculumRepository curriculumRepository;

	private final CourseStudentRepository courseStudentRepository;

	private final ExamRepository examRepository;

	private final ExamQuestionRepository examQuestionRepository;

	private final AttendanceRepository attendanceRepository;

	private final ExamResultRepository examResultRepository;

	private final AssignmentRepository assignmentRepository;

	private final SubmissionRepository submissionRepository;

	private final CourseContentRepository courseContentRepository;

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

		createCourseContents(courses);

		// 5. 수강 신청 데이터 생성
		// 첫 번째 강의: 모든 학생 수강
		List<CourseStudent> courseStudents1 = createCourseStudents(courses.get(0), students);
		// 두 번째 강의: 절반의 학생만 수강
		List<CourseStudent> courseStudents2 = createCourseStudents(courses.get(1),
				students.subList(0, students.size() / 2));

		// 8. 출석 데이터 생성 (첫 번째 강의에 대해서만)
		createAttendances(courseStudents1);

		// 6. 시험 데이터 생성 (시험과 문제 생성)
		createExams(courses);

		// 7. 시험 결과 데이터 생성 (모든 학생이 모든 시험을 봄)
		createExamResults(courses, students);

		// 9. 과제 데이터 생성 (첫 번째 강의에 대해서만)
		createAssignments(courses.get(0), instructors.get(0));

	}

	// 강사 추가를 위해 list로 반환
	private List<Instructor> createInstructors() {
		List<Instructor> instructors = new ArrayList<>();
		Instructor instructor1 = Instructor.createInstructor(
				"instructor@test.com",
				"윤강사"
		);

		Instructor instructor2 = Instructor.createInstructor(
				"instructor2@test.com",
				"김강사"
		);

		Instructor instructor3 = Instructor.createInstructor(
				"instructor3@test.com",
				"이강사"
		);

		instructors.add(instructor1);
		instructors.add(instructor2);
		instructors.add(instructor3);

		return instructorRepository.saveAll(instructors);
	}

	private List<Student> createStudents() {
		List<Student> students = new ArrayList<>();
		for (int i = 1; i <= 10; i++) {
			Student student = Student.createStudent(
					"student" + i + "@test.com",
					"학생" + i
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

	private void createExams(List<Course> courses) {
		// AWS 과정 시험들
		Course awsCourse = courses.get(0);

		// 1. Java 중간고사
		Exam javaExam = Exam.builder()
				.title("Java 중간고사")
				.examDateTime(LocalDateTime.of(2025, 11, 24, 18, 00))
				.duration(120)
				.totalScore(100)
				.description("Java 기초 문법과 객체지향 프로그래밍에 대한 이해도를 평가합니다.")
				.course(awsCourse)
				.build();
		examRepository.save(javaExam);

		// Java 시험 문제들
		createJavaExamQuestions(javaExam);

		// 2. Spring 중간고사
		Exam springExam = Exam.builder()
				.title("Spring Framework 중간고사")
				.examDateTime(LocalDateTime.of(2025, 9, 13, 14, 0))
				.duration(120)
				.totalScore(100)
				.description("Spring Framework의 핵심 개념과 JPA에 대한 이해도를 평가합니다.")
				.course(awsCourse)
				.build();
		examRepository.save(springExam);

		createSpringExamQuestions(springExam);

		// 풀스택 과정 시험들
		Course fullstackCourse = courses.get(1);

		// 3. JavaScript 시험
		Exam jsExam = Exam.builder()
				.title("JavaScript & ES6+ 평가")
				.examDateTime(LocalDateTime.of(2025, 8, 30, 14, 0))
				.duration(90)
				.totalScore(100)
				.description("JavaScript 기초와 ES6+ 문법에 대한 이해도를 평가합니다.")
				.course(awsCourse)
				.build();
		examRepository.save(jsExam);

		createJavaScriptExamQuestions(jsExam);

		// 4. React 시험
		Exam reactExam = Exam.builder()
				.title("React 실전 평가")
				.examDateTime(LocalDateTime.of(2025, 9, 27, 14, 0))
				.duration(150)
				.totalScore(100)
				.description("React의 핵심 개념과 실전 응용력을 평가합니다.")
				.course(awsCourse)
				.build();
		examRepository.save(reactExam);

		createReactExamQuestions(reactExam);
	}

	private void createJavaExamQuestions(Exam exam) {
		// 객관식 문제 10개 (각 6점 = 60점)
		MultipleChoiceQuestion q1 = MultipleChoiceQuestion.builder()
				.questionText("Java의 기본 데이터 타입이 아닌 것은?")
				.examNumber(1)
				.score(6)
				.exam(exam)
				.correctAnswer("3")
				.build();
		addChoicesToQuestion(q1, List.of(
				new String[] {"1", "int"},
				new String[] {"2", "boolean"},
				new String[] {"3", "String"},
				new String[] {"4", "char"}
		));

		MultipleChoiceQuestion q2 = MultipleChoiceQuestion.builder()
				.questionText("다음 중 객체 지향의 특징이 아닌 것은?")
				.examNumber(2)
				.score(6)
				.exam(exam)
				.correctAnswer("4")
				.build();
		addChoicesToQuestion(q2, List.of(
				new String[] {"1", "캡슐화"},
				new String[] {"2", "상속성"},
				new String[] {"3", "다형성"},
				new String[] {"4", "순차성"}
		));

		MultipleChoiceQuestion q3 = MultipleChoiceQuestion.builder()
				.questionText("Java에서 배열의 길이를 알기 위한 속성은?")
				.examNumber(3)
				.score(6)
				.exam(exam)
				.correctAnswer("2")
				.build();
		addChoicesToQuestion(q3, List.of(
				new String[] {"1", "size"},
				new String[] {"2", "length"},
				new String[] {"3", "count"},
				new String[] {"4", "index"}
		));

		MultipleChoiceQuestion q4 = MultipleChoiceQuestion.builder()
				.questionText("다음 중 접근 제어자의 범위가 가장 넓은 것은?")
				.examNumber(4)
				.score(6)
				.exam(exam)
				.correctAnswer("1")
				.build();
		addChoicesToQuestion(q4, List.of(
				new String[] {"1", "public"},
				new String[] {"2", "protected"},
				new String[] {"3", "default"},
				new String[] {"4", "private"}
		));

		MultipleChoiceQuestion q5 = MultipleChoiceQuestion.builder()
				.questionText("Java에서 상수를 선언하기 위한 키워드는?")
				.examNumber(5)
				.score(6)
				.exam(exam)
				.correctAnswer("3")
				.build();
		addChoicesToQuestion(q5, List.of(
				new String[] {"1", "constant"},
				new String[] {"2", "static"},
				new String[] {"3", "final"},
				new String[] {"4", "const"}
		));

		MultipleChoiceQuestion q6 = MultipleChoiceQuestion.builder()
				.questionText("다음 중 추상 클래스의 특징이 아닌 것은?")
				.examNumber(6)
				.score(6)
				.exam(exam)
				.correctAnswer("4")
				.build();
		addChoicesToQuestion(q6, List.of(
				new String[] {"1", "추상 메소드를 포함할 수 있다"},
				new String[] {"2", "상속받은 클래스에서 구현해야 한다"},
				new String[] {"3", "abstract 키워드를 사용한다"},
				new String[] {"4", "다중 상속이 가능하다"}
		));

		MultipleChoiceQuestion q7 = MultipleChoiceQuestion.builder()
				.questionText("Java의 Collection Framework에서 순서가 있는 데이터의 집합은?")
				.examNumber(7)
				.score(6)
				.exam(exam)
				.correctAnswer("2")
				.build();
		addChoicesToQuestion(q7, List.of(
				new String[] {"1", "Set"},
				new String[] {"2", "List"},
				new String[] {"3", "Map"},
				new String[] {"4", "Queue"}
		));

		MultipleChoiceQuestion q8 = MultipleChoiceQuestion.builder()
				.questionText("예외 처리를 위한 키워드가 아닌 것은?")
				.examNumber(8)
				.score(6)
				.exam(exam)
				.correctAnswer("4")
				.build();
		addChoicesToQuestion(q8, List.of(
				new String[] {"1", "try"},
				new String[] {"2", "catch"},
				new String[] {"3", "finally"},
				new String[] {"4", "finish"}
		));

		MultipleChoiceQuestion q9 = MultipleChoiceQuestion.builder()
				.questionText("Java 8에서 추가된 기능이 아닌 것은?")
				.examNumber(9)
				.score(6)
				.exam(exam)
				.correctAnswer("3")
				.build();
		addChoicesToQuestion(q9, List.of(
				new String[] {"1", "Lambda Expression"},
				new String[] {"2", "Stream API"},
				new String[] {"3", "Generics"},
				new String[] {"4", "Optional"}
		));

		MultipleChoiceQuestion q10 = MultipleChoiceQuestion.builder()
				.questionText("다음 중 Thread의 상태가 아닌 것은?")
				.examNumber(10)
				.score(6)
				.exam(exam)
				.correctAnswer("4")
				.build();
		addChoicesToQuestion(q10, List.of(
				new String[] {"1", "NEW"},
				new String[] {"2", "RUNNABLE"},
				new String[] {"3", "WAITING"},
				new String[] {"4", "DESTROY"}
		));

		// 주관식 문제 5개 (각 8점 = 40점)
		SubjectiveQuestion q11 = SubjectiveQuestion.builder()
				.questionText("자바의 메모리 영역 중 객체가 생성되는 영역의 이름은?")
				.examNumber(11)
				.score(8)
				.exam(exam)
				.correctAnswer("Heap")
				.build();

		SubjectiveQuestion q12 = SubjectiveQuestion.builder()
				.questionText("인터페이스의 모든 메소드는 기본적으로 어떤 접근 제어자를 가지는가?")
				.examNumber(12)
				.score(8)
				.exam(exam)
				.correctAnswer("public")
				.build();

		SubjectiveQuestion q13 = SubjectiveQuestion.builder()
				.questionText("Java에서 여러 스레드가 공유 자원에 접근하는 것을 제어하기 위한 키워드는?")
				.examNumber(13)
				.score(8)
				.exam(exam)
				.correctAnswer("synchronized")
				.build();

		SubjectiveQuestion q14 = SubjectiveQuestion.builder()
				.questionText("Java 8에서 도입된, null을 안전하게 다루기 위한 클래스의 이름은?")
				.examNumber(14)
				.score(8)
				.exam(exam)
				.correctAnswer("Optional")
				.build();

		SubjectiveQuestion q15 = SubjectiveQuestion.builder()
				.questionText("Java의 가비지 컬렉션을 직접 실행하기 위해 호출하는 메소드는?")
				.examNumber(15)
				.score(8)
				.exam(exam)
				.correctAnswer("System.gc")
				.build();

		examQuestionRepository.saveAll(List.of(q1, q2, q3, q4, q5, q6, q7, q8, q9, q10,
				q11, q12, q13, q14, q15));
	}

	private void createSpringExamQuestions(Exam exam) {
		MultipleChoiceQuestion q1 = MultipleChoiceQuestion.builder()
				.questionText("Spring Framework의 핵심 개념이 아닌 것은?")
				.examNumber(1)
				.score(5)
				.exam(exam)
				.correctAnswer("3")
				.build();

		addChoicesToQuestion(q1, List.of(
				new String[] {"1", "IoC (Inversion of Control)"},
				new String[] {"2", "DI (Dependency Injection)"},
				new String[] {"3", "GC (Garbage Collection)"},
				new String[] {"4", "AOP (Aspect Oriented Programming)"}
		));

		MultipleChoiceQuestion q2 = MultipleChoiceQuestion.builder()
				.questionText("Bean의 기본 Scope는?")
				.examNumber(2)
				.score(5)
				.exam(exam)
				.correctAnswer("1")
				.build();

		addChoicesToQuestion(q2, List.of(
				new String[] {"1", "singleton"},
				new String[] {"2", "prototype"},
				new String[] {"3", "request"},
				new String[] {"4", "session"}
		));

		MultipleChoiceQuestion q3 = MultipleChoiceQuestion.builder()
				.questionText("다음 중 Spring Boot의 특징이 아닌 것은?")
				.examNumber(3)
				.score(5)
				.exam(exam)
				.correctAnswer("4")
				.build();

		addChoicesToQuestion(q3, List.of(
				new String[] {"1", "내장 서버 제공"},
				new String[] {"2", "자동 설정(Auto Configuration)"},
				new String[] {"3", "의존성 관리"},
				new String[] {"4", "수동 설정 필수"}
		));

		MultipleChoiceQuestion q4 = MultipleChoiceQuestion.builder()
				.questionText("@Autowired의 주입 방식이 아닌 것은?")
				.examNumber(4)
				.score(5)
				.exam(exam)
				.correctAnswer("4")
				.build();

		addChoicesToQuestion(q4, List.of(
				new String[] {"1", "생성자 주입"},
				new String[] {"2", "필드 주입"},
				new String[] {"3", "세터 주입"},
				new String[] {"4", "메서드 반환값 주입"}
		));

		MultipleChoiceQuestion q5 = MultipleChoiceQuestion.builder()
				.questionText("Spring MVC에서 Controller의 반환값으로 적절하지 않은 것은?")
				.examNumber(5)
				.score(5)
				.exam(exam)
				.correctAnswer("3")
				.build();

		addChoicesToQuestion(q5, List.of(
				new String[] {"1", "String"},
				new String[] {"2", "ModelAndView"},
				new String[] {"3", "InputStream"},
				new String[] {"4", "ResponseEntity"}
		));

		MultipleChoiceQuestion q6 = MultipleChoiceQuestion.builder()
				.questionText("Spring Security에서 인증(Authentication)을 저장하는 곳은?")
				.examNumber(6)
				.score(5)
				.exam(exam)
				.correctAnswer("2")
				.build();

		addChoicesToQuestion(q6, List.of(
				new String[] {"1", "HttpSession"},
				new String[] {"2", "SecurityContext"},
				new String[] {"3", "HttpServletRequest"},
				new String[] {"4", "Cookie"}
		));

		MultipleChoiceQuestion q7 = MultipleChoiceQuestion.builder()
				.questionText("JPA의 영속성 컨텍스트 상태가 아닌 것은?")
				.examNumber(7)
				.score(5)
				.exam(exam)
				.correctAnswer("4")
				.build();

		addChoicesToQuestion(q7, List.of(
				new String[] {"1", "비영속"},
				new String[] {"2", "영속"},
				new String[] {"3", "준영속"},
				new String[] {"4", "임시영속"}
		));

		MultipleChoiceQuestion q8 = MultipleChoiceQuestion.builder()
				.questionText("Spring AOP의 Advice 종류가 아닌 것은?")
				.examNumber(8)
				.score(5)
				.exam(exam)
				.correctAnswer("3")
				.build();

		addChoicesToQuestion(q8, List.of(
				new String[] {"1", "Before"},
				new String[] {"2", "After"},
				new String[] {"3", "During"},
				new String[] {"4", "Around"}
		));

		MultipleChoiceQuestion q9 = MultipleChoiceQuestion.builder()
				.questionText("@Transactional의 전파 속성 기본값은?")
				.examNumber(9)
				.score(5)
				.exam(exam)
				.correctAnswer("1")
				.build();

		addChoicesToQuestion(q9, List.of(
				new String[] {"1", "REQUIRED"},
				new String[] {"2", "REQUIRES_NEW"},
				new String[] {"3", "SUPPORTS"},
				new String[] {"4", "MANDATORY"}
		));

		MultipleChoiceQuestion q10 = MultipleChoiceQuestion.builder()
				.questionText("Spring Boot에서 외부 설정 우선순위가 가장 높은 것은?")
				.examNumber(10)
				.score(5)
				.exam(exam)
				.correctAnswer("2")
				.build();

		addChoicesToQuestion(q10, List.of(
				new String[] {"1", "application.properties"},
				new String[] {"2", "Command Line Arguments"},
				new String[] {"3", "환경 변수"},
				new String[] {"4", "application.yml"}
		));

		MultipleChoiceQuestion q11 = MultipleChoiceQuestion.builder()
				.questionText("Spring Data JPA에서 제공하지 않는 메서드는?")
				.examNumber(11)
				.score(5)
				.exam(exam)
				.correctAnswer("4")
				.build();

		addChoicesToQuestion(q11, List.of(
				new String[] {"1", "findById"},
				new String[] {"2", "save"},
				new String[] {"3", "deleteById"},
				new String[] {"4", "updateById"}
		));

		MultipleChoiceQuestion q12 = MultipleChoiceQuestion.builder()
				.questionText("Spring MVC의 구성 요소가 아닌 것은?")
				.examNumber(12)
				.score(5)
				.exam(exam)
				.correctAnswer("3")
				.build();

		addChoicesToQuestion(q12, List.of(
				new String[] {"1", "DispatcherServlet"},
				new String[] {"2", "HandlerMapping"},
				new String[] {"3", "EntityManager"},
				new String[] {"4", "ViewResolver"}
		));

		MultipleChoiceQuestion q13 = MultipleChoiceQuestion.builder()
				.questionText("Spring Boot Actuator에서 제공하지 않는 정보는?")
				.examNumber(13)
				.score(5)
				.exam(exam)
				.correctAnswer("4")
				.build();

		addChoicesToQuestion(q13, List.of(
				new String[] {"1", "health"},
				new String[] {"2", "metrics"},
				new String[] {"3", "env"},
				new String[] {"4", "users"}
		));

		MultipleChoiceQuestion q14 = MultipleChoiceQuestion.builder()
				.questionText("Spring의 빈 생명주기 콜백 메서드가 아닌 것은?")
				.examNumber(14)
				.score(5)
				.exam(exam)
				.correctAnswer("3")
				.build();

		addChoicesToQuestion(q14, List.of(
				new String[] {"1", "@PostConstruct"},
				new String[] {"2", "@PreDestroy"},
				new String[] {"3", "@PreCreate"},
				new String[] {"4", "InitializingBean"}
		));

		MultipleChoiceQuestion q15 = MultipleChoiceQuestion.builder()
				.questionText("Spring Security의 인증 필터 순서로 올바른 것은?")
				.examNumber(15)
				.score(5)
				.exam(exam)
				.correctAnswer("2")
				.build();

		addChoicesToQuestion(q15, List.of(
				new String[] {"1", "Basic -> Form -> JWT"},
				new String[] {"2", "JWT -> Basic -> Form"},
				new String[] {"3", "Form -> JWT -> Basic"},
				new String[] {"4", "Basic -> JWT -> Form"}
		));

		// 주관식 문제 5개
		SubjectiveQuestion q16 = SubjectiveQuestion.builder()
				.questionText("Spring Framework에서 Bean을 등록하기 위한 대표적인 어노테이션은?")
				.examNumber(16)
				.score(5)
				.exam(exam)
				.correctAnswer("@Component")
				.build();

		SubjectiveQuestion q17 = SubjectiveQuestion.builder()
				.questionText("Spring MVC에서 클라이언트의 요청을 최초로 받는 서블릿의 이름은?")
				.examNumber(17)
				.score(5)
				.exam(exam)
				.correctAnswer("DispatcherServlet")
				.build();

		SubjectiveQuestion q18 = SubjectiveQuestion.builder()
				.questionText("JPA에서 엔티티의 기본키를 자동생성할 때 사용하는 어노테이션은?")
				.examNumber(18)
				.score(5)
				.exam(exam)
				.correctAnswer("@GeneratedValue")
				.build();

		SubjectiveQuestion q19 = SubjectiveQuestion.builder()
				.questionText("Spring Security에서 비밀번호를 암호화하는 대표적인 인코더는?")
				.examNumber(19)
				.score(5)
				.exam(exam)
				.correctAnswer("BCryptPasswordEncoder")
				.build();

		SubjectiveQuestion q20 = SubjectiveQuestion.builder()
				.questionText("Spring Boot에서 자동 설정을 비활성화할 때 사용하는 어노테이션은?")
				.examNumber(20)
				.score(5)
				.exam(exam)
				.correctAnswer("@EnableAutoConfiguration")
				.build();

		examQuestionRepository.saveAll(List.of(q1, q2, q3, q4, q5, q6, q7, q8, q9, q10,
				q11, q12, q13, q14, q15, q16, q17, q18, q19, q20));
	}

	private void createJavaScriptExamQuestions(Exam exam) {
		// 객관식 문제 10개 (각 6점 = 60점)
		MultipleChoiceQuestion q1 = MultipleChoiceQuestion.builder()
				.questionText("JavaScript에서 변수를 선언하는 키워드가 아닌 것은?")
				.examNumber(1)
				.score(6)
				.exam(exam)
				.correctAnswer("4")
				.build();
		addChoicesToQuestion(q1, List.of(
				new String[] {"1", "var"},
				new String[] {"2", "let"},
				new String[] {"3", "const"},
				new String[] {"4", "variable"}
		));

		MultipleChoiceQuestion q2 = MultipleChoiceQuestion.builder()
				.questionText("다음 중 JavaScript의 원시 타입이 아닌 것은?")
				.examNumber(2)
				.score(6)
				.exam(exam)
				.correctAnswer("3")
				.build();
		addChoicesToQuestion(q2, List.of(
				new String[] {"1", "number"},
				new String[] {"2", "string"},
				new String[] {"3", "array"},
				new String[] {"4", "boolean"}
		));

		MultipleChoiceQuestion q3 = MultipleChoiceQuestion.builder()
				.questionText("JavaScript에서 비동기 처리를 위한 객체는?")
				.examNumber(3)
				.score(6)
				.exam(exam)
				.correctAnswer("2")
				.build();
		addChoicesToQuestion(q3, List.of(
				new String[] {"1", "Callback"},
				new String[] {"2", "Promise"},
				new String[] {"3", "Async"},
				new String[] {"4", "Wait"}
		));

		MultipleChoiceQuestion q4 = MultipleChoiceQuestion.builder()
				.questionText("ES6에서 추가된 기능이 아닌 것은?")
				.examNumber(4)
				.score(6)
				.exam(exam)
				.correctAnswer("4")
				.build();
		addChoicesToQuestion(q4, List.of(
				new String[] {"1", "let/const"},
				new String[] {"2", "Arrow Function"},
				new String[] {"3", "Template Literals"},
				new String[] {"4", "typeof"}
		));

		MultipleChoiceQuestion q5 = MultipleChoiceQuestion.builder()
				.questionText("JavaScript에서 DOM 요소를 선택하는 메서드가 아닌 것은?")
				.examNumber(5)
				.score(6)
				.exam(exam)
				.correctAnswer("3")
				.build();
		addChoicesToQuestion(q5, List.of(
				new String[] {"1", "getElementById"},
				new String[] {"2", "querySelector"},
				new String[] {"3", "selectElement"},
				new String[] {"4", "getElementsByClassName"}
		));

		MultipleChoiceQuestion q6 = MultipleChoiceQuestion.builder()
				.questionText("다음 중 이벤트 버블링을 막는 메서드는?")
				.examNumber(6)
				.score(6)
				.exam(exam)
				.correctAnswer("2")
				.build();
		addChoicesToQuestion(q6, List.of(
				new String[] {"1", "preventDefault()"},
				new String[] {"2", "stopPropagation()"},
				new String[] {"3", "stopBubbling()"},
				new String[] {"4", "cancelBubble()"}
		));

		MultipleChoiceQuestion q7 = MultipleChoiceQuestion.builder()
				.questionText("JavaScript에서 배열을 순회하는 메서드가 아닌 것은?")
				.examNumber(7)
				.score(6)
				.exam(exam)
				.correctAnswer("4")
				.build();
		addChoicesToQuestion(q7, List.of(
				new String[] {"1", "map"},
				new String[] {"2", "forEach"},
				new String[] {"3", "filter"},
				new String[] {"4", "loop"}
		));

		MultipleChoiceQuestion q8 = MultipleChoiceQuestion.builder()
				.questionText("JavaScript에서 객체의 속성을 동적으로 접근하는 방법은?")
				.examNumber(8)
				.score(6)
				.exam(exam)
				.correctAnswer("1")
				.build();
		addChoicesToQuestion(q8, List.of(
				new String[] {"1", "obj[key]"},
				new String[] {"2", "obj->key"},
				new String[] {"3", "obj::key"},
				new String[] {"4", "obj@key"}
		));

		MultipleChoiceQuestion q9 = MultipleChoiceQuestion.builder()
				.questionText("다음 중 JavaScript의 스코프가 아닌 것은?")
				.examNumber(9)
				.score(6)
				.exam(exam)
				.correctAnswer("3")
				.build();
		addChoicesToQuestion(q9, List.of(
				new String[] {"1", "Global scope"},
				new String[] {"2", "Function scope"},
				new String[] {"3", "Package scope"},
				new String[] {"4", "Block scope"}
		));

		MultipleChoiceQuestion q10 = MultipleChoiceQuestion.builder()
				.questionText("JavaScript에서 모듈을 가져오는 키워드는?")
				.examNumber(10)
				.score(6)
				.exam(exam)
				.correctAnswer("2")
				.build();
		addChoicesToQuestion(q10, List.of(
				new String[] {"1", "require"},
				new String[] {"2", "import"},
				new String[] {"3", "include"},
				new String[] {"4", "using"}
		));

		// 주관식 문제 5개 (각 8점 = 40점)
		SubjectiveQuestion q11 = SubjectiveQuestion.builder()
				.questionText("JavaScript에서 비동기 함수를 정의할 때 사용하는 키워드는?")
				.examNumber(11)
				.score(8)
				.exam(exam)
				.correctAnswer("async")
				.build();

		SubjectiveQuestion q12 = SubjectiveQuestion.builder()
				.questionText("JavaScript에서 Promise가 성공적으로 완료되었을 때 호출되는 메서드는?")
				.examNumber(12)
				.score(8)
				.exam(exam)
				.correctAnswer("then")
				.build();

		SubjectiveQuestion q13 = SubjectiveQuestion.builder()
				.questionText("JavaScript에서 객체의 속성과 값을 한번에 추출하는 문법은?")
				.examNumber(13)
				.score(8)
				.exam(exam)
				.correctAnswer("destructuring")
				.build();

		SubjectiveQuestion q14 = SubjectiveQuestion.builder()
				.questionText("JavaScript에서 여러 개의 Promise를 동시에 처리하는 메서드는?")
				.examNumber(14)
				.score(8)
				.exam(exam)
				.correctAnswer("Promise.all")
				.build();

		SubjectiveQuestion q15 = SubjectiveQuestion.builder()
				.questionText("JavaScript에서 모든 타입을 문자열로 변환하는 메서드는?")
				.examNumber(15)
				.score(8)
				.exam(exam)
				.correctAnswer("toString")
				.build();

		examQuestionRepository.saveAll(List.of(q1, q2, q3, q4, q5, q6, q7, q8, q9, q10,
				q11, q12, q13, q14, q15));
	}

	private void createReactExamQuestions(Exam exam) {
		// 객관식 문제 15개 (각 4점 = 60점)
		MultipleChoiceQuestion q1 = MultipleChoiceQuestion.builder()
				.questionText("React에서 컴포넌트의 상태를 관리하는 Hook은?")
				.examNumber(1)
				.score(4)
				.exam(exam)
				.correctAnswer("1")
				.build();
		addChoicesToQuestion(q1, List.of(
				new String[] {"1", "useState"},
				new String[] {"2", "useStatus"},
				new String[] {"3", "useState()"},
				new String[] {"4", "setState"}
		));

		MultipleChoiceQuestion q2 = MultipleChoiceQuestion.builder()
				.questionText("React 컴포넌트의 생명주기와 관련된 Hook은?")
				.examNumber(2)
				.score(4)
				.exam(exam)
				.correctAnswer("2")
				.build();
		addChoicesToQuestion(q2, List.of(
				new String[] {"1", "useLife"},
				new String[] {"2", "useEffect"},
				new String[] {"3", "useCycle"},
				new String[] {"4", "useLifecycle"}
		));

		MultipleChoiceQuestion q3 = MultipleChoiceQuestion.builder()
				.questionText("React에서 컴포넌트 간 데이터 전달 방식이 아닌 것은?")
				.examNumber(3)
				.score(4)
				.exam(exam)
				.correctAnswer("4")
				.build();
		addChoicesToQuestion(q3, List.of(
				new String[] {"1", "Props"},
				new String[] {"2", "Context"},
				new String[] {"3", "Redux"},
				new String[] {"4", "Direct Binding"}
		));

		MultipleChoiceQuestion q4 = MultipleChoiceQuestion.builder()
				.questionText("React 컴포넌트의 특징이 아닌 것은?")
				.examNumber(4)
				.score(4)
				.exam(exam)
				.correctAnswer("3")
				.build();
		addChoicesToQuestion(q4, List.of(
				new String[] {"1", "재사용성"},
				new String[] {"2", "단방향 데이터 흐름"},
				new String[] {"3", "양방향 바인딩"},
				new String[] {"4", "선언적 UI"}
		));

		MultipleChoiceQuestion q5 = MultipleChoiceQuestion.builder()
				.questionText("React에서 조건부 렌더링에 사용되는 연산자가 아닌 것은?")
				.examNumber(5)
				.score(4)
				.exam(exam)
				.correctAnswer("4")
				.build();
		addChoicesToQuestion(q5, List.of(
				new String[] {"1", "&&"},
				new String[] {"2", "?:"},
				new String[] {"3", "||"},
				new String[] {"4", "??"}
		));

		MultipleChoiceQuestion q6 = MultipleChoiceQuestion.builder()
				.questionText("React Router에서 동적 라우팅을 위한 문법은?")
				.examNumber(6)
				.score(4)
				.exam(exam)
				.correctAnswer("1")
				.build();
		addChoicesToQuestion(q6, List.of(
				new String[] {"1", ":parameter"},
				new String[] {"2", "*parameter"},
				new String[] {"3", "@parameter"},
				new String[] {"4", "#parameter"}
		));

		MultipleChoiceQuestion q7 = MultipleChoiceQuestion.builder()
				.questionText("React에서 불변성을 지키기 위한 메서드가 아닌 것은?")
				.examNumber(7)
				.score(4)
				.exam(exam)
				.correctAnswer("4")
				.build();
		addChoicesToQuestion(q7, List.of(
				new String[] {"1", "map"},
				new String[] {"2", "filter"},
				new String[] {"3", "concat"},
				new String[] {"4", "push"}
		));

		MultipleChoiceQuestion q8 = MultipleChoiceQuestion.builder()
				.questionText("React에서 성능 최적화를 위한 Hook은?")
				.examNumber(8)
				.score(4)
				.exam(exam)
				.correctAnswer("2")
				.build();
		addChoicesToQuestion(q8, List.of(
				new String[] {"1", "usePerformance"},
				new String[] {"2", "useMemo"},
				new String[] {"3", "useOptimize"},
				new String[] {"4", "useSpeed"}
		));

		MultipleChoiceQuestion q9 = MultipleChoiceQuestion.builder()
				.questionText("React 컴포넌트에서 ref를 사용하기 위한 Hook은?")
				.examNumber(9)
				.score(4)
				.exam(exam)
				.correctAnswer("1")
				.build();
		addChoicesToQuestion(q9, List.of(
				new String[] {"1", "useRef"},
				new String[] {"2", "useReference"},
				new String[] {"3", "createRef"},
				new String[] {"4", "makeRef"}
		));

		MultipleChoiceQuestion q10 = MultipleChoiceQuestion.builder()
				.questionText("React에서 사용되는 가상 DOM의 특징이 아닌 것은?")
				.examNumber(10)
				.score(4)
				.exam(exam)
				.correctAnswer("3")
				.build();
		addChoicesToQuestion(q10, List.of(
				new String[] {"1", "메모리에 존재"},
				new String[] {"2", "실제 DOM과 비교"},
				new String[] {"3", "직접 조작 가능"},
				new String[] {"4", "성능 최적화"}
		));

		MultipleChoiceQuestion q11 = MultipleChoiceQuestion.builder()
				.questionText("React에서 컴포넌트를 최적화하는 방법이 아닌 것은?")
				.examNumber(11)
				.score(4)
				.exam(exam)
				.correctAnswer("4")
				.build();
		addChoicesToQuestion(q11, List.of(
				new String[] {"1", "React.memo"},
				new String[] {"2", "useMemo"},
				new String[] {"3", "useCallback"},
				new String[] {"4", "useOptimize"}
		));

		MultipleChoiceQuestion q12 = MultipleChoiceQuestion.builder()
				.questionText("React에서 비동기 데이터 처리를 위한 Hook은?")
				.examNumber(12)
				.score(4)
				.exam(exam)
				.correctAnswer("1")
				.build();
		addChoicesToQuestion(q12, List.of(
				new String[] {"1", "useEffect"},
				new String[] {"2", "useAsync"},
				new String[] {"3", "usePromise"},
				new String[] {"4", "useData"}
		));

		MultipleChoiceQuestion q13 = MultipleChoiceQuestion.builder()
				.questionText("React에서 폼 데이터를 관리하는 방식이 아닌 것은?")
				.examNumber(13)
				.score(4)
				.exam(exam)
				.correctAnswer("3")
				.build();
		addChoicesToQuestion(q13, List.of(
				new String[] {"1", "제어 컴포넌트"},
				new String[] {"2", "비제어 컴포넌트"},
				new String[] {"3", "자동 제어 컴포넌트"},
				new String[] {"4", "ref 사용"}
		));

		MultipleChoiceQuestion q14 = MultipleChoiceQuestion.builder()
				.questionText("React에서 Context를 사용하기 위한 Hook은?")
				.examNumber(14)
				.score(4)
				.exam(exam)
				.correctAnswer("2")
				.build();
		addChoicesToQuestion(q14, List.of(
				new String[] {"1", "useContextProvider"},
				new String[] {"2", "useContext"},
				new String[] {"3", "useProvider"},
				new String[] {"4", "useGlobal"}
		));

		MultipleChoiceQuestion q15 = MultipleChoiceQuestion.builder()
				.questionText("React에서 컴포넌트의 props 타입을 체크하는 라이브러리는?")
				.examNumber(15)
				.score(4)
				.exam(exam)
				.correctAnswer("1")
				.build();
		addChoicesToQuestion(q15, List.of(
				new String[] {"1", "PropTypes"},
				new String[] {"2", "TypeChecker"},
				new String[] {"3", "PropsValidator"},
				new String[] {"4", "ReactTypes"}
		));

		// 주관식 문제 5개 (각 8점 = 40점)
		SubjectiveQuestion q16 = SubjectiveQuestion.builder()
				.questionText("React에서 컴포넌트를 감싸는 최상위 요소의 이름은?")
				.examNumber(16)
				.score(8)
				.exam(exam)
				.correctAnswer("Fragment")
				.build();

		SubjectiveQuestion q17 = SubjectiveQuestion.builder()
				.questionText("React에서 부모 컴포넌트로 데이터를 전달하기 위해 사용하는 함수 타입 prop의 일반적인 접두사는?")
				.examNumber(17)
				.score(8)
				.exam(exam)
				.correctAnswer("on")
				.build();

		SubjectiveQuestion q18 = SubjectiveQuestion.builder()
				.questionText("React에서 컴포넌트의 props 변화를 감지하여 재렌더링을 방지하는 고차 컴포넌트(HOC)는?")
				.examNumber(18)
				.score(8)
				.exam(exam)
				.correctAnswer("memo")
				.build();

		SubjectiveQuestion q19 = SubjectiveQuestion.builder()
				.questionText("React에서 사이드 이펙트를 처리하기 위한 Hook의 의존성 배열에 빈 배열을 넣으면 언제 실행되는가?")
				.examNumber(19)
				.score(8)
				.exam(exam)
				.correctAnswer("mount")
				.build();

		SubjectiveQuestion q20 = SubjectiveQuestion.builder()
				.questionText("React에서 컴포넌트 트리 전체에 데이터를 제공하는 기능의 이름은?")
				.examNumber(20)
				.score(8)
				.exam(exam)
				.correctAnswer("Context")
				.build();

		examQuestionRepository.saveAll(List.of(q1, q2, q3, q4, q5, q6, q7, q8, q9, q10,
				q11, q12, q13, q14, q15, q16, q17, q18, q19, q20));
	}

	private void addChoicesToQuestion(MultipleChoiceQuestion question, List<String[]> choices) {
		for (String[] choice : choices) {
			Choice choiceEntity = Choice.builder()
					.number(Integer.parseInt(choice[0]))
					.choiceText(choice[1])
					.build();
			question.addChoice(choiceEntity);
		}
	}

	// 시험 결과
	private void createExamResults(List<Course> courses, List<Student> students) {
		Random random = new Random();
		List<ExamResult> allResults = new ArrayList<>();

		// 각 강의의 시험들을 개별적으로 처리
		for (Course course : courses) {
			List<Exam> exams = examRepository.findByCourseId(course.getId());

			for (Exam exam : exams) {
				// 각 시험에 대해 개별적으로 questions를 로드
				List<ExamQuestion> questions = examQuestionRepository.findByExamIdOrderByExamNumberAsc(exam.getId());

				for (Student student : students) {
					ExamResult result = ExamResult.builder()
							.exam(exam)
							.student(student)
							.build();

					for (ExamQuestion question : questions) {
						boolean isCorrect = random.nextInt(100) < 70;
						String studentAnswer = generateRandomAnswer(random, question, isCorrect);

						ExamScore score = ExamScore.builder()
								.question(question)
								.studentAnswer(studentAnswer)
								.build();

						if (question.validateAnswer(studentAnswer)) {
							score.updateScore(question.getScore());
						} else {
							score.updateScore(0);
						}

						result.addScore(score);
					}

					allResults.add(result);
				}
			}
		}

		log.info("Attempting to save {} exam results", allResults.size());
		examResultRepository.saveAll(allResults);
		log.info("Finished saving exam results");
	}

	private String generateRandomAnswer(Random random, ExamQuestion question, boolean isCorrect) {
		if (isCorrect) {
			return question.getCorrectAnswer();
		}

		if (question instanceof MultipleChoiceQuestion) {
			// 객관식인 경우 정답을 제외한 1-4 중 랜덤 선택
			String correctAnswer = question.getCorrectAnswer();
			String randomWrongAnswer;
			do {
				randomWrongAnswer = String.valueOf(random.nextInt(4) + 1);
			} while (randomWrongAnswer.equals(correctAnswer));
			return randomWrongAnswer;
		} else {
			// 주관식인 경우 오답 목록에서 랜덤 선택
			String[] wrongAnswers = {
					"wrong", "incorrect", "unknown", "undefined", "null",
					"false", "error", "none", "empty", "default"
			};
			return wrongAnswers[random.nextInt(wrongAnswers.length)];
		}
	}

	private void createAttendances(List<CourseStudent> courseStudents) {
		LocalDate startDate = LocalDate.of(2024, 7, 3); // 강의 시작일
		LocalDate endDate = LocalDate.of(2025, 1, 7);  // 강의 종료일

		List<Attendance> attendances = new ArrayList<>();
		Random random = new Random();

		for (CourseStudent courseStudent : courseStudents) {
			LocalDate currentDate = startDate;
			while (!currentDate.isAfter(endDate)) {
				// 주말이 아닌 경우에만 출석 데이터 생성
				if (!isWeekend(currentDate)) {
					// 80% 출석, 10% 지각, 5% 결석, 5% 병결
					int randomValue = random.nextInt(100);
					AttendanceStatus status;

					if (randomValue < 80) {
						status = AttendanceStatus.PRESENT;
					} else if (randomValue < 90) {
						status = AttendanceStatus.LATE;
					} else if (randomValue < 95) {
						status = AttendanceStatus.ABSENT;
					} else {
						status = AttendanceStatus.SICK_LEAVE;
					}

					Attendance attendance = Attendance.builder()
							.courseStudent(courseStudent)
							.date(currentDate)
							.build();
					attendance.updateStatus(status);
					attendances.add(attendance);
				}
				currentDate = currentDate.plusDays(1);
			}
		}

		attendanceRepository.saveAll(attendances);
	}

	private void createAssignments(Course course, Instructor instructor) {
		List<Assignment> assignments = new ArrayList<>();

		// 1. Java 기초 과제
		assignments.add(Assignment.createAssignment(
				"Java 객체지향 프로그래밍 실습",
				"상속, 다형성, 캡슐화를 활용한 간단한 도서관 관리 시스템을 구현하세요.",
				LocalDateTime.of(2024, 7, 20, 23, 59),
				course,
				instructor
		));

		// 2. 알고리즘 과제
		assignments.add(Assignment.createAssignment(
				"알고리즘 문제 풀이",
				"정렬 알고리즘을 구현하고 시간복잡도를 분석하세요.",
				LocalDateTime.of(2024, 8, 10, 23, 59),
				course,
				instructor
		));

		// 3. 데이터베이스 설계 과제
		assignments.add(Assignment.createAssignment(
				"데이터베이스 모델링",
				"쇼핑몰 데이터베이스를 설계하고 ERD를 작성하세요.",
				LocalDateTime.of(2024, 8, 30, 23, 59),
				course,
				instructor
		));

		// 4. Spring Framework 과제
		assignments.add(Assignment.createAssignment(
				"Spring MVC 게시판 구현",
				"Spring Boot와 JPA를 활용하여 CRUD 기능이 있는 게시판을 구현하세요.",
				LocalDateTime.of(2024, 9, 20, 23, 59),
				course,
				instructor
		));

		// 5. AWS 실습 과제
		assignments.add(Assignment.createAssignment(
				"AWS 서비스 구축",
				"EC2, RDS, S3를 활용하여 웹 애플리케이션을 배포하세요.",
				LocalDateTime.of(2024, 10, 10, 23, 59),
				course,
				instructor
		));

		assignments = assignmentRepository.saveAll(assignments);
		createSubmissions(assignments, studentRepository.findAll());
	}

	private void createSubmissions(List<Assignment> assignments, List<Student> students) {
		Random random = new Random();
		List<Submission> allSubmissions = new ArrayList<>();

		for (Assignment assignment : assignments) {
			for (Student student : students) {
				// 90%의 확률로 과제 제출
				if (random.nextInt(100) < 90) {
					SubmissionStatus status;
					SubmissionGrade grade = null;
					String feedback = null;

					// 과제 제출 날짜가 지났는지 확인
					boolean isAfterDeadline = LocalDateTime.now().isAfter(assignment.getDeadline());

					if (isAfterDeadline) {
						// 제출 기한이 지난 과제는 채점 완료 상태
						status = SubmissionStatus.GRADED;

						// 랜덤하게 성적 부여 (70% PASS, 30% NONE_PASS)
						if (random.nextInt(100) < 70) {
							grade = SubmissionGrade.PASS;
							feedback = "잘 작성된 과제입니다. 특히 " + generatePositiveFeedback();
						} else {
							grade = SubmissionGrade.NONE_PASS;
							feedback = "아쉬운 점이 있습니다. " + generateNegativeFeedback();
						}
					} else {
						// 제출 기한이 지나지 않은 과제
						if (random.nextInt(100) < 70) {
							// 70%는 채점 중
							status = SubmissionStatus.SUBMITTED;
							grade = SubmissionGrade.UNDER_REVIEW;
						} else {
							// 30%는 채점 완료
							status = SubmissionStatus.GRADED;
							if (random.nextInt(100) < 80) {
								grade = SubmissionGrade.PASS;
								feedback = "잘 작성된 과제입니다. 특히 " + generatePositiveFeedback();
							} else {
								grade = SubmissionGrade.NONE_PASS;
								feedback = "아쉬운 점이 있습니다. " + generateNegativeFeedback();
							}
						}
					}

					Submission submission = Submission.createSubmission(
							generateSubmissionDescription(assignment.getTitle()),
							assignment,
							student
					);

					if (grade != null && feedback != null) {
						submission.updateGrade(grade, feedback);
					}

					// 파일 생성은 생략 (실제 파일이 필요한 경우 S3 업로드 로직 구현 필요)
					allSubmissions.add(submission);
				}
			}
		}

		submissionRepository.saveAll(allSubmissions);
	}

	private String generateSubmissionDescription(String assignmentTitle) {
		return String.format("%s에 대한 과제 제출입니다. 열심히 수행하였습니다.", assignmentTitle);
	}

	private String generatePositiveFeedback() {
		String[] positivePoints = {
				"코드의 구조가 잘 정리되어 있습니다.",
				"문제 해결 방식이 창의적입니다.",
				"제시된 요구사항을 모두 충족했습니다.",
				"코드 품질이 우수합니다.",
				"문서화가 잘 되어있습니다."
		};
		return positivePoints[new Random().nextInt(positivePoints.length)];
	}

	private String generateNegativeFeedback() {
		String[] negativePoints = {
				"코드 구조의 개선이 필요합니다.",
				"일부 요구사항이 누락되었습니다.",
				"테스트 케이스가 부족합니다.",
				"예외 처리가 미흡합니다.",
				"코드 재사용성을 고려해야 합니다."
		};
		return negativePoints[new Random().nextInt(negativePoints.length)];
	}

	// 강의 자료 생성
	private void createCourseContents(List<Course> courses) {
		// AWS 과정 강의 자료 생성
		createAwsCourseContents(courses.get(0));

		// 풀스택 과정 강의 자료 생성
		createFullstackCourseContents(courses.get(0));
	}

	private void createAwsCourseContents(Course course) {
		// 1. Java & Database (2024-07-03 ~ 2024-07-23)
		createContent(course, LocalDate.of(2024, 7, 3),
				"Java 기초 - 변수와 데이터 타입",
				"Java의 기본 문법과 변수 선언, 데이터 타입에 대해 학습합니다. 기본형과 참조형 변수의 차이점과 형변환에 대해 다룹니다.");

		createContent(course, LocalDate.of(2024, 7, 5),
				"Java 객체지향 프로그래밍 - 클래스와 객체",
				"객체지향의 핵심 개념과 클래스 설계 방법에 대해 학습합니다. 캡슐화, 상속, 다형성의 개념과 실제 활용 사례를 다룹니다.");

		createContent(course, LocalDate.of(2024, 7, 8),
				"Java 컬렉션 프레임워크",
				"List, Set, Map 등 주요 컬렉션의 특징과 사용법을 학습합니다. 각 자료구조의 성능 특성과 적절한 사용 시기를 다룹니다.");

		createContent(course, LocalDate.of(2024, 7, 10),
				"Java Stream API와 람다식",
				"함수형 프로그래밍과 Stream API의 활용법을 학습합니다. 람다식을 이용한 간결한 코드 작성과 데이터 처리 방법을 다룹니다.");

		createContent(course, LocalDate.of(2024, 7, 12),
				"Database 기초와 SQL",
				"관계형 데이터베이스의 기본 개념과 SQL 문법을 학습합니다. DDL, DML, DCL의 차이와 실제 쿼리 작성 방법을 다룹니다.");

		// 2. Front-End (2024-07-24 ~ 2024-08-14)
		createContent(course, LocalDate.of(2024, 7, 24),
				"HTML5 기초와 시맨틱 마크업",
				"웹 표준과 시맨틱 태그의 활용법을 학습합니다. 접근성과 SEO를 고려한 마크업 작성 방법을 다룹니다.");

		createContent(course, LocalDate.of(2024, 7, 26),
				"CSS3 레이아웃과 반응형 웹",
				"Flexbox, Grid 및 미디어 쿼리를 활용한 반응형 웹 디자인을 학습합니다. 모바일 퍼스트 접근법과 브라우저 호환성을 다룹니다.");

		createContent(course, LocalDate.of(2024, 7, 29),
				"JavaScript ES6+ 기초",
				"모던 자바스크립트의 주요 문법과 활용법을 학습합니다. 변수 스코프, 호이스팅, 클로저의 개념을 다룹니다.");

		createContent(course, LocalDate.of(2024, 8, 1),
				"React 컴포넌트와 Props",
				"React의 기본 개념과 컴포넌트 설계 방법을 학습합니다. Props를 통한 데이터 전달과 컴포넌트 재사용성을 다룹니다.");

		// 3. Spring Boot (2024-08-19 ~ 2024-09-13)
		createContent(course, LocalDate.of(2024, 8, 19),
				"Spring Boot 핵심 원리",
				"Spring Boot의 동작 원리와 주요 기능을 학습합니다. 자동 설정, 의존성 관리, 내장 서버의 특징을 다룹니다.");

		createContent(course, LocalDate.of(2024, 8, 21),
				"Spring Data JPA 활용",
				"JPA를 이용한 데이터 접근 계층 구현 방법을 학습합니다. 엔티티 매핑, 연관관계 설정, 쿼리 메소드를 다룹니다.");

		createContent(course, LocalDate.of(2024, 8, 23),
				"Spring Security와 인증/인가",
				"보안 설정과 JWT 기반 인증 구현 방법을 학습합니다. OAuth2.0 소셜 로그인과 권한 관리를 다룹니다.");

		// 4. Linux & Network (2024-09-19 ~ 2024-09-25)
		createContent(course, LocalDate.of(2024, 9, 19),
				"Linux 기본 명령어와 쉘 스크립트",
				"리눅스 시스템 관리와 쉘 스크립트 작성법을 학습합니다. 파일 시스템, 프로세스 관리, 네트워크 설정을 다룹니다.");

		createContent(course, LocalDate.of(2024, 9, 20),
				"네트워크 프로토콜과 보안",
				"TCP/IP 프로토콜과 네트워크 보안 기초를 학습합니다. OSI 7계층, HTTP/HTTPS, 방화벽 설정을 다룹니다.");

		// 5. Docker & Kubernetes (2024-09-26 ~ 2024-10-07)
		createContent(course, LocalDate.of(2024, 9, 26),
				"Docker 컨테이너 기초",
				"Docker의 기본 개념과 컨테이너 운영 방법을 학습합니다. Dockerfile 작성, 이미지 빌드, 컨테이너 배포를 다룹니다.");

		createContent(course, LocalDate.of(2024, 9, 28),
				"Kubernetes 클러스터 운영",
				"K8s 클러스터 구축과 운영 방법을 학습합니다. Pod, Service, Deployment 등 주요 리소스 관리를 다룹니다.");

		// 6. AWS 서비스 (2024-10-08 ~ 2024-10-17)
		createContent(course, LocalDate.of(2024, 10, 8),
				"AWS EC2와 VPC 구성",
				"AWS 기본 인프라 구성 방법을 학습합니다. EC2 인스턴스 관리, VPC 네트워크 설정, 보안 그룹 구성을 다룹니다.");

		createContent(course, LocalDate.of(2024, 10, 10),
				"AWS ECS와 EKS 활용",
				"AWS 컨테이너 서비스 운영 방법을 학습합니다. ECS 클러스터 구성, EKS 워크로드 배포, 오토스케일링을 다룹니다.");

		// 7. MSA (2024-10-18 ~ 2024-10-29)
		createContent(course, LocalDate.of(2024, 10, 18),
				"MSA 설계 원칙과 패턴",
				"마이크로서비스 아키텍처의 핵심 개념을 학습합니다. 서비스 분리, 통신 방식, 데이터 관리 전략을 다룹니다.");

		createContent(course, LocalDate.of(2024, 10, 21),
				"Spring Cloud 활용",
				"Spring Cloud를 이용한 MSA 구현 방법을 학습합니다. Service Discovery, Config Server, API Gateway 등의 구성 요소를 다룹니다.");
	}

	private void createFullstackCourseContents(Course course) {
		// 1. JavaScript & ES6+ (2024-08-05 ~ 2024-08-30)
		createContent(course, LocalDate.of(2024, 8, 5),
				"JavaScript 기초와 DOM",
				"자바스크립트의 기본 문법과 DOM 조작 방법을 학습합니다. 이벤트 처리와 DOM API의 주요 메서드를 실습합니다.");

		createContent(course, LocalDate.of(2024, 8, 7),
				"ES6+ 새로운 기능",
				"화살표 함수, 구조 분해, 모듈 시스템 등 ES6+의 새로운 기능을 학습합니다. 실제 프로젝트에서의 활용 사례를 다룹니다.");

		createContent(course, LocalDate.of(2024, 8, 9),
				"비동기 프로그래밍",
				"Promise, async/await를 활용한 비동기 처리를 학습합니다. 콜백 지옥 해결과 에러 처리 방법을 다룹니다.");

		createContent(course, LocalDate.of(2024, 8, 12),
				"함수형 프로그래밍",
				"순수 함수와 불변성 원칙을 학습합니다. 고차 함수, 클로저, 커링의 개념과 활용법을 다룹니다.");

		createContent(course, LocalDate.of(2024, 8, 14),
				"TypeScript 기초",
				"TypeScript의 기본 문법과 타입 시스템을 학습합니다. 인터페이스, 제네릭, 데코레이터의 활용법을 다룹니다.");

		// 2. React (2024-09-02 ~ 2024-09-27)
		createContent(course, LocalDate.of(2024, 9, 2),
				"React Hooks 활용",
				"useState, useEffect 등 주요 Hook의 활용법을 학습합니다. 커스텀 Hook 작성과 성능 최적화를 다룹니다.");

		createContent(course, LocalDate.of(2024, 9, 4),
				"상태 관리와 Redux",
				"Redux를 이용한 전역 상태 관리를 학습합니다. 액션, 리듀서, 미들웨어의 개념과 구현 방법을 다룹니다.");

		createContent(course, LocalDate.of(2024, 9, 6),
				"React Router와 SPA",
				"라우팅 설정과 SPA 구현 방법을 학습합니다. 동적 라우팅, 중첩 라우팅, 보호된 라우트를 다룹니다.");

		createContent(course, LocalDate.of(2024, 9, 9),
				"성능 최적화",
				"메모이제이션과 렌더링 최적화 기법을 학습합니다. React.memo, useMemo, useCallback의 활용법을 다룹니다.");

		createContent(course, LocalDate.of(2024, 9, 11),
				"테스트와 디버깅",
				"React 컴포넌트 테스트 방법을 학습합니다. Jest와 React Testing Library를 이용한 단위 테스트를 다룹니다.");

		// 3. Spring Boot & JPA (2024-09-30 ~ 2024-10-25)
		createContent(course, LocalDate.of(2024, 9, 30),
				"Spring Boot 기초",
				"스프링 부트의 핵심 기능과 설정 방법을 학습합니다. 의존성 관리, 자동 설정, 프로파일 관리를 다룹니다.");

		createContent(course, LocalDate.of(2024, 10, 2),
				"JPA 엔티티 매핑",
				"JPA 엔티티 설계와 연관관계 매핑을 학습합니다. 영속성 컨텍스트, 지연 로딩, 캐시 전략을 다룹니다.");

		createContent(course, LocalDate.of(2024, 10, 4),
				"Spring Data JPA",
				"리포지토리 설계와 쿼리 메소드 활용법을 학습합니다. 페이징, 정렬, 벌크 연산의 구현 방법을 다룹니다.");

		createContent(course, LocalDate.of(2024, 10, 7),
				"트랜잭션 관리",
				"스프링의 트랜잭션 관리 방법을 학습합니다. 전파 속성, 격리 수준, 롤백 처리를 다룹니다.");

		// 4. REST API (2024-10-28 ~ 2024-11-22)
		createContent(course, LocalDate.of(2024, 10, 28),
				"REST API 설계",
				"RESTful API 설계 원칙과 방법론을 학습합니다. 리소스 모델링, URL 설계, HTTP 메소드 활용을 다룹니다.");

		createContent(course, LocalDate.of(2024, 10, 30),
				"API 문서화",
				"Swagger를 이용한 API 문서 자동화를 학습합니다. OpenAPI 스펙, API 테스트, 문서 배포 방법을 다룹니다.");

		createContent(course, LocalDate.of(2024, 11, 1),
				"예외 처리와 검증",
				"API 예외 처리와 입력값 검증 방법을 학습합니다. @Valid, ExceptionHandler, 커스텀 검증 로직을 다룹니다.");

		// 5. 보안 (2024-11-25 ~ 2024-12-20)
		createContent(course, LocalDate.of(2024, 11, 25),
				"Spring Security 기초",
				"인증/인가 처리와 보안 설정을 학습합니다. SecurityFilterChain, UserDetailsService, PasswordEncoder를 다룹니다.");

		createContent(course, LocalDate.of(2024, 11, 27),
				"JWT 인증",
				"토큰 기반 인증 구현 방법을 학습합니다. JWT 생성/검증, RefreshToken, 보안 취약점 대응을 다룹니다.");

		createContent(course, LocalDate.of(2024, 11, 29),
				"OAuth2.0 소셜 로그인",
				"소셜 로그인 구현 방법을 학습합니다. OAuth2.0 흐름, 리소스 서버 연동, 사용자 정보 처리를 다룹니다.");

		// 6. 프로젝트 (2024-12-23 ~ 2025-02-28)
		createContent(course, LocalDate.of(2024, 12, 23),
				"프로젝트 기획",
				"요구사항 분석과 프로젝트 범위 설정 방법을 학습합니다. 유스케이스 작성, WBS 작성, 일정 관리를 다룹니다.");

		createContent(course, LocalDate.of(2024, 12, 26),
				"Git 협업 전략",
				"Git Flow 전략과 효과적인 협업 방식을 학습합니다. 브랜치 전략, 코드 리뷰, 충돌 해결 방법을 다룹니다.");
	}

	private void createContent(
			Course course,
			LocalDate date,
			String title,
			String content
	) {
		CourseContent courseContent = CourseContent.builder()
				.title(title)
				.content(content)
				.date(date)
				.course(course)
				.build();

		courseContentRepository.save(courseContent);
	}

}
