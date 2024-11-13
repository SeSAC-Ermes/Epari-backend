package com.example.epari.assignment.service;

import com.example.epari.assignment.domain.Assignment;
import com.example.epari.assignment.domain.AssignmentFile;
import com.example.epari.assignment.dto.assignment.AssignmentRequestDto;
import com.example.epari.assignment.dto.assignment.AssignmentResponseDto;
import com.example.epari.assignment.repository.AssignmentRepository;
import com.example.epari.course.domain.Course;
import com.example.epari.course.repository.CourseRepository;
import com.example.epari.global.common.service.S3FileService;
import com.example.epari.user.domain.Instructor;
import com.example.epari.user.repository.InstructorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

// 로그를 간단하게 보여주는 어노테이션
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssignmentService {

	private final AssignmentRepository assignmentRepository;

	private final CourseRepository courseRepository;

	private final InstructorRepository instructorRepository;

	private final S3FileService s3FileService;


	/**
	 * 과제 추가
	 */
	@Transactional
	public AssignmentResponseDto addAssignment(Long courseId, AssignmentRequestDto requestDto, Long instructorId) {
		Course course = courseRepository.findById(courseId)
				.orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

		Instructor instructor = instructorRepository.findById(instructorId)
				.orElseThrow(() -> new IllegalArgumentException("강사 정보를 찾을 수 없습니다."));

		// 해당 강좌의 담당 강사인지 확인
		if (!course.getInstructor().getId().equals(instructor.getId())) {
			throw new IllegalArgumentException("해당 강의의 담당 강사가 아닙니다.");
		}

		Assignment assignment = Assignment.createAssignment(
				requestDto.getTitle(),
				requestDto.getDescription(),
				requestDto.getDeadline(),
				course,
				instructor
		);

		// 파일 업로드 처리
		if (requestDto.getFiles() != null && !requestDto.getFiles().isEmpty()) {
			for (MultipartFile file : requestDto.getFiles()) {
				String fileUrl = s3FileService.uploadFile("assignments", file);

				AssignmentFile assignmentFile = AssignmentFile.createAssignmentFile(
						file.getOriginalFilename(),
						extractStoredFileName(fileUrl),
						fileUrl,
						file.getSize(),
						assignment
				);

				assignment.addFile(assignmentFile);
			}
		}


		return AssignmentResponseDto.from(assignmentRepository.save(assignment));
	}


	/**
	 * 전체 과제 조회
	 */
	public List<AssignmentResponseDto> getAssignmentsByCourse(Long courseId) {
		return assignmentRepository.findByCourseIdWithInstructor(courseId).stream()
				.map(AssignmentResponseDto::from)
				.collect(Collectors.toList());
	}

	/**
	 * 입력 키워드를 포함하는 과제 조회
	 */
	public List<AssignmentResponseDto> getAssignmentsByTitle(String title) {
		return assignmentRepository.findAssignmentByTitleContains(title).stream()
				.map(AssignmentResponseDto::from)
				.collect(Collectors.toList());
	}

	/**
	 * 과제 상세 조회
	 */
	public AssignmentResponseDto getAssignmentById(Long courseId, Long assignmentId) {
		Assignment assignment = assignmentRepository.findByIdWithInstructor(assignmentId)
				.orElseThrow(() -> new IllegalArgumentException("과제를 찾을 수 없습니다."));

		// 해당 과제가 요청된 코스에 속하는지 확인
		if (!assignment.getCourse().getId().equals(courseId)) {
			throw new IllegalArgumentException("해당 강의의 과제가 아닙니다.");
		}

		return AssignmentResponseDto.from(assignment);
	}

	/**
	 * 과제 수정
	 */
	@Transactional
	public AssignmentResponseDto updateAssignment(Long assignmentId, AssignmentRequestDto requestDto, Long instructorId) {
		Assignment assignment = assignmentRepository.findByIdWithInstructor(assignmentId)
				.orElseThrow(() -> new IllegalArgumentException("과제를 찾을 수 없습니다."));

		// 수정 권한 검증
		if (!assignment.getInstructor().getId().equals(instructorId)) {
			throw new IllegalArgumentException("해당 과제의 수정 권한이 없습니다.");
		}

		assignment.updateAssignment(
				requestDto.getTitle(),
				requestDto.getDescription(),
				requestDto.getDeadline()
		);

		return AssignmentResponseDto.from(assignment);
	}


	/**
	 * 과제 삭제
	 */
	@Transactional
	public void deleteAssignment(Long assignmentId, Long instructorId) {
		Assignment assignment = assignmentRepository.findByIdWithInstructor(assignmentId)
				.orElseThrow(() -> new IllegalArgumentException("과제를 찾을 수 없습니다."));

		// 삭제 권한 검증
		if (!assignment.getInstructor().getId().equals(instructorId)) {
			throw new IllegalArgumentException("해당 과제의 삭제 권한이 없습니다.");
		}

		assignmentRepository.delete(assignment);
	}

	private String extractStoredFileName(String fileUrl) {
		return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
	}

}
