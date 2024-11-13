package com.example.epari.assignment.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.example.epari.course.domain.Course;
import com.example.epari.course.repository.CourseRepository;
import com.example.epari.user.domain.Instructor;
import com.example.epari.user.repository.InstructorRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.epari.assignment.domain.Assignment;
import com.example.epari.assignment.domain.AssignmentFile;
import com.example.epari.assignment.dto.assignment.AssignmentRequestDto;
import com.example.epari.assignment.dto.assignment.AssignmentResponseDto;
import com.example.epari.assignment.dto.file.AssignmentFileResponseDto;
import com.example.epari.assignment.repository.AssignmentFileRepository;
import com.example.epari.assignment.repository.AssignmentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 로그를 간단하게 보여주는 어노테이션
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssignmentService {

	@Value("${file.upload.path}")  // application.properties에서 설정
	private String fileUploadPath;

	private final AssignmentRepository assignmentRepository;

	private final AssignmentFileRepository assignmentFileRepository;

	private final CourseRepository courseRepository;

	private final InstructorRepository instructorRepository;

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

	@Transactional
	public List<AssignmentFileResponseDto> uploadFiles(List<MultipartFile> files, Long assignmentId) {
		Assignment assignment = null;
		if (assignmentId != null) {
			assignment = assignmentRepository.findById(assignmentId)
					.orElseThrow(() -> new IllegalArgumentException("과제를 찾을 수 없습니다."));
		}

		List<AssignmentFile> uploadedFiles = new ArrayList<>();

		try {
			Path uploadPath = Paths.get(fileUploadPath);
			if (!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
			}

			for (MultipartFile file : files) {
				if (file.isEmpty())
					continue;

				String originalFileName = file.getOriginalFilename();
				String storedFileName = UUID.randomUUID().toString() + "_" + originalFileName;
				Path filePath = uploadPath.resolve(storedFileName);

				Files.copy(file.getInputStream(), filePath);

				AssignmentFile assignmentFile = AssignmentFile.createAssignmentFile(
						originalFileName,
						storedFileName,
						"/uploads/" + storedFileName,
						file.getSize(),
						assignment
				);

				AssignmentFile savedFile = assignmentFileRepository.save(assignmentFile);
				if (assignment != null) {
					assignment.addFile(savedFile);
				}
				uploadedFiles.add(savedFile);
			}

			return uploadedFiles.stream()
					.map(AssignmentFileResponseDto::new)
					.collect(Collectors.toList());

		} catch (IOException e) {
			log.error("파일 업로드 중 오류 발생", e);
			throw new RuntimeException("파일 업로드에 실패했습니다: " + e.getMessage());
		}
	}

	/**
	 * 파일 삭제
	 */
	@Transactional
	public void deleteFile(Long fileId) {
		AssignmentFile file = assignmentFileRepository.findById(fileId)
				.orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다."));

		try {
			Path filePath = Paths.get(fileUploadPath).resolve(file.getStoredFileName());
			Files.deleteIfExists(filePath);
			assignmentFileRepository.delete(file);
		} catch (IOException e) {
			log.error("파일 삭제 중 오류 발생", e);
			throw new RuntimeException("파일 삭제에 실패했습니다: " + e.getMessage());
		}
	}

}
