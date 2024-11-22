package com.example.epari.admin.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.epari.admin.dto.AvailableStudentResponseDTO;
import com.example.epari.admin.dto.CourseStudentResponseDTO;
import com.example.epari.admin.dto.CourseStudentUpdateRequestDTO;
import com.example.epari.admin.exception.CourseNotFoundException;
import com.example.epari.admin.repository.AdminCourseStudentRepository;
import com.example.epari.admin.repository.AdminStudentRepository;
import com.example.epari.course.domain.Course;
import com.example.epari.course.domain.CourseStudent;
import com.example.epari.course.repository.CourseRepository;
import com.example.epari.user.domain.Student;

import lombok.RequiredArgsConstructor;

/**
 * 관리자가 강의와 학생 관련 정보를 관리하는 비즈니스 로직을 처리하는 서비스 클래스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCourseStudentService {

	private final AdminCourseStudentRepository courseStudentRepository;

	private final AdminStudentRepository studentRepository;

	private final CourseRepository courseRepository;

	/**
	 * 강의에 등록된 수강생 목록 조회
	 */
	public List<CourseStudentResponseDTO> getEnrolledStudents(Long courseId) {
		return courseStudentRepository.findEnrolledStudentsByCourseId(courseId);
	}

	/**
	 * 강의에 등록 가능한 수강생 목록 조회
	 */
	public List<AvailableStudentResponseDTO> getAvailableStudents(Long courseId, String keyword) {
		return studentRepository.findAvailableStudentsForCourse(courseId, keyword);
	}

	/**
	 * 수강생 목록 업데이트
	 */
	@Transactional
	@CacheEvict(value = "courses", key = "'all'")
	public void updateEnrolledStudents(Long courseId, CourseStudentUpdateRequestDTO request) {
		Course course = courseRepository.findById(courseId)
				.orElseThrow(CourseNotFoundException::new);

		// 1. 현재 등록된 수강생 ID 집합
		Set<Long> currentStudentIds = courseStudentRepository
				.findByCourseId(courseId)
				.stream()
				.map(cs -> cs.getStudent().getId())
				.collect(Collectors.toSet());

		// 2. 새로운 수강생 ID 집합
		Set<Long> newStudentIds = new HashSet<>(request.getStudentIds());

		// 3. 추가될 학생들 계산 (새 목록에만 있는 ID)
		Set<Long> toAdd = newStudentIds.stream()
				.filter(id -> !currentStudentIds.contains(id))
				.collect(Collectors.toSet());

		// 4. 제거될 학생들 계산 (기존 목록에만 있는 ID)
		Set<Long> toRemove = currentStudentIds.stream()
				.filter(id -> !newStudentIds.contains(id))
				.collect(Collectors.toSet());

		// 5. 제거 처리
		if (!toRemove.isEmpty()) {
			courseStudentRepository.deleteByCourseIdAndStudentIdIn(courseId, toRemove);
		}

		// 6. 추가 처리
		if (!toAdd.isEmpty()) {
			List<Student> studentsToAdd = studentRepository.findAllById(toAdd);
			List<CourseStudent> newEnrollments = studentsToAdd.stream()
					.map(student -> new CourseStudent(course, student))
					.collect(Collectors.toList());
			courseStudentRepository.saveAll(newEnrollments);
		}
	}

}
