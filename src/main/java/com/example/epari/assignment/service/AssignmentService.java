package com.example.epari.assignment.service;

import com.example.epari.assignment.domain.Assignment;
import com.example.epari.assignment.domain.AssignmentFile;
import com.example.epari.assignment.dto.assignment.AssignmentRequestDto;
import com.example.epari.assignment.dto.assignment.AssignmentResponseDto;
import com.example.epari.assignment.repository.AssignmentFileRepository;
import com.example.epari.assignment.repository.AssignmentRepository;
import com.example.epari.course.domain.Course;
import com.example.epari.course.repository.CourseRepository;
import com.example.epari.global.common.base.BaseUser;
import com.example.epari.global.common.repository.BaseUserRepository;
import com.example.epari.global.common.service.S3FileService;
import com.example.epari.user.domain.Instructor;
import com.example.epari.user.repository.InstructorRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
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

	private final BaseUserRepository baseUserRepository;

	private final S3FileService s3FileService;

	private final AssignmentFileRepository assignmentFileRepository;

	/**
	 * 과제 추가
	 */
	@Transactional
	public AssignmentResponseDto addAssignment(Long courseId, AssignmentRequestDto requestDto, String email) {
		BaseUser user = baseUserRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

		Course course = courseRepository.findById(courseId)
				.orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

		Instructor instructor = instructorRepository.findById(user.getId())
				.orElseThrow(() -> new IllegalArgumentException("강사 정보를 찾을 수 없습니다."));

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
	public AssignmentResponseDto updateAssignment(Long courseId, Long assignmentId, AssignmentRequestDto requestDto,
			String email) {
		BaseUser user = baseUserRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

		Course course = courseRepository.findById(courseId)
				.orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

		Assignment assignment = assignmentRepository.findByIdWithInstructor(assignmentId)
				.orElseThrow(() -> new IllegalArgumentException("과제를 찾을 수 없습니다."));

		Instructor instructor = instructorRepository.findById(user.getId())
				.orElseThrow(() -> new IllegalArgumentException("강사 정보를 찾을 수 없습니다."));

		if (!course.getInstructor().getId().equals(instructor.getId())) {
			throw new IllegalArgumentException("해당 강의의 담당 강사가 아닙니다.");
		}

		assignment.updateAssignment(
				requestDto.getTitle(),
				requestDto.getDescription(),
				requestDto.getDeadline()
		);

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

		return AssignmentResponseDto.from(assignment);
	}

	/**
	 * 과제 삭제
	 */
	@Transactional
	public void deleteAssignment(Long assignmentId, String email) {
		BaseUser user = baseUserRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

		Assignment assignment = assignmentRepository.findByIdWithInstructor(assignmentId)
				.orElseThrow(() -> new IllegalArgumentException("과제를 찾을 수 없습니다."));

		if (!assignment.getInstructor().getId().equals(user.getId())) {
			throw new IllegalArgumentException("해당 과제의 삭제 권한이 없습니다.");
		}

		for (AssignmentFile assignmentFile : assignment.getFiles()) {
			try {
				s3FileService.deleteFile(assignmentFile.getFileUrl());
			} catch (Exception e) {
				log.error("S3에서 파일 삭제를 실패했습니다.", assignmentFile.getFileUrl(), e);
			}
		}

		assignmentRepository.delete(assignment);
	}

	/**
	 * 파일 다운로드
	 */
	public String downloadFile(Long courseId, Long assignmentId, Long fileId) {
		// 과제 확인
		Assignment assignment = assignmentRepository.findByIdAndCourseId(assignmentId, courseId)
				.orElseThrow(() -> new IllegalArgumentException("과제 자료를 찾을 수 없습니다."));

		// 파일 확인
		AssignmentFile assignmentFile = assignment.getFiles().stream()
				.filter(f -> f.getId().equals(fileId))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다."));

		// 7일간 유효한 다운로드 링크
		return s3FileService.generatePresignedUrl(assignmentFile.getFileUrl(), Duration.ofDays(7));
	}

	/**
	 * 특정 파일 삭제
	 */
	@Transactional
	public AssignmentResponseDto deleteFile(Long courseId, Long assignmentId, Long fileId) {
		Assignment assignment = assignmentRepository.findByIdAndCourseId(assignmentId, courseId)
				.orElseThrow(() -> new IllegalArgumentException("과제 파일을 찾을 수 없습니다."));

		AssignmentFile file = assignment.getFiles().stream()
				.filter(f -> f.getId().equals(fileId))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다."));

		try {
			s3FileService.deleteFile(file.getFileUrl());
		} catch (Exception e) {
			log.error("S3에서 파일 삭제를 실패했습니다: {}", file.getFileUrl(), e);
		}

		//과제에서 파일 제거
		assignment.removeFile(file);

		return AssignmentResponseDto.from(assignment);
	}

	private String extractStoredFileName(String fileUrl) {
		return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
	}

}
