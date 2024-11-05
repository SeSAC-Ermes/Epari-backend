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

import com.example.epari.lecture.domain.Attendance;
import com.example.epari.lecture.domain.Curriculum;
import com.example.epari.lecture.domain.Lecture;
import com.example.epari.lecture.domain.LectureStudent;
import com.example.epari.lecture.repository.AttendanceRepository;
import com.example.epari.lecture.repository.CurriculumRepository;
import com.example.epari.lecture.repository.LectureRepository;
import com.example.epari.lecture.repository.LectureStudentRepository;
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

	private final LectureRepository lectureRepository;

	private final CurriculumRepository curriculumRepository;

	private final AttendanceRepository attendanceRepository;

	private final LectureStudentRepository lectureStudentRepository;

	@Transactional
	@Override
	public void run(ApplicationArguments args) {
		// 1. 강사 생성
		Instructor instructor = createInstructor();

		// 2. 학생 20명 생성
		List<Student> students = createStudents();

		// 3. 강의 생성
		Lecture lecture = createLecture(instructor);

		// 4. 커리큘럼 생성
		createCurriculums(lecture);

		// 5. 수강 신청 데이터 생성
		List<LectureStudent> lectureStudents = createLectureStudents(lecture, students);

		// 6. 출석 데이터 초기화
		initializeAttendances(lectureStudents);
	}

	private Instructor createInstructor() {
		Instructor instructor = Instructor.createInstructor(
				"instructor@test.com",
				"1234",
				"윤강사",
				"010-1234-5678"
		);
		return instructorRepository.save(instructor);
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

	private Lecture createLecture(Instructor instructor) {
		LocalDate startDate = LocalDate.of(2024, 7, 3);  // 2024년 7월 3일 시작
		LocalDate endDate = LocalDate.of(2025, 1, 7);    // 2025년 1월 7일 종료

		Lecture lecture = Lecture.builder()
				.name("AWS 클라우드를 활용한 MSA 기반 자바 개발자 양성 과정")
				.instructor(instructor)
				.startDate(startDate)
				.endDate(endDate)
				.classroom("교육장 301호")
				.build();

		return lectureRepository.save(lecture);
	}

	private void createCurriculums(Lecture lecture) {
		List<Curriculum> curriculums = new ArrayList<>();
		Map<LocalDate, CurriculumInfo> topicsByDate = getTopicsByDate();

		LocalDate currentDate = lecture.getStartDate();
		while (!currentDate.isAfter(lecture.getEndDate())) {
			if (topicsByDate.containsKey(currentDate)) {
				CurriculumInfo info = topicsByDate.get(currentDate);
				Curriculum curriculum = Curriculum.builder()
						.lecture(lecture)
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

	private List<LectureStudent> createLectureStudents(Lecture lecture, List<Student> students) {
		List<LectureStudent> lectureStudents = new ArrayList<>();
		for (Student student : students) {
			LectureStudent lectureStudent = new LectureStudent(lecture, student);
			lectureStudents.add(lectureStudent);
		}
		return lectureStudentRepository.saveAll(lectureStudents);
	}

	private Map<LocalDate, CurriculumInfo> getTopicsByDate() {
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

	private void initializeAttendances(List<LectureStudent> lectureStudents) {
		for (LectureStudent lectureStudent : lectureStudents) {
			LocalDate currentDate = lectureStudent.getLecture().getStartDate();
			LocalDate endDate = lectureStudent.getLecture().getEndDate();

			while (!currentDate.isAfter(endDate)) {
				if (!isWeekend(currentDate)) {
					Attendance attendance = Attendance.builder()
							.lectureStudent(lectureStudent)
							.date(currentDate)
							.build();
					attendanceRepository.save(attendance);
				}
				currentDate = currentDate.plusDays(1);
			}
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
