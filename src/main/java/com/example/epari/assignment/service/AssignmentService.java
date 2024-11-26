package com.example.epari.assignment.service;

import com.example.epari.admin.exception.CourseNotFoundException;
import com.example.epari.assignment.domain.Assignment;
import com.example.epari.assignment.domain.AssignmentFile;
import com.example.epari.assignment.domain.Submission;
import com.example.epari.assignment.domain.SubmissionFile;
import com.example.epari.assignment.dto.assignment.AssignmentRequestDto;
import com.example.epari.assignment.dto.assignment.AssignmentResponseDto;
import com.example.epari.assignment.repository.AssignmentRepository;
import com.example.epari.assignment.repository.SubmissionRepository;
import com.example.epari.course.domain.Course;
import com.example.epari.course.repository.CourseRepository;
import com.example.epari.global.common.base.BaseUser;
import com.example.epari.global.common.repository.BaseUserRepository;
import com.example.epari.global.common.service.S3FileService;
import com.example.epari.global.exception.assignment.AssignmentAccessDeniedException;
import com.example.epari.global.exception.assignment.AssignmentInvalidException;
import com.example.epari.global.exception.assignment.AssignmentNotFoundException;
import com.example.epari.global.exception.auth.AuthUserNotFoundException;
import com.example.epari.global.exception.auth.InstructorNotFoundException;
import com.example.epari.global.exception.course.CourseInstructorMismatchException;
import com.example.epari.global.exception.file.AssignmentFileNotFoundException;
import com.example.epari.global.exception.file.FileDeleteFailedException;
import com.example.epari.global.exception.file.FileUploadFailedException;
import com.example.epari.user.domain.Instructor;
import com.example.epari.user.repository.InstructorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
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

	private final SubmissionRepository submissionRepository;

	/**
	 * 과제 추가
	 */
	@Transactional
	public AssignmentResponseDto addAssignment(Long courseId, AssignmentRequestDto requestDto, String email) {
		BaseUser user = baseUserRepository.findByEmail(email)
				.orElseThrow(AuthUserNotFoundException::new);

		Course course = courseRepository.findById(courseId)
				.orElseThrow(CourseNotFoundException::new);

		Instructor instructor = instructorRepository.findById(user.getId())
				.orElseThrow(InstructorNotFoundException::new);

		if (!course.getInstructor().getId().equals(instructor.getId())) {
			throw new CourseInstructorMismatchException();
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
				.orElseThrow(AssignmentNotFoundException::new);

		// 해당 과제가 요청된 코스에 속하는지 확인
		if (!assignment.getCourse().getId().equals(courseId)) {
			throw new AssignmentInvalidException();
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
				.orElseThrow(AuthUserNotFoundException::new);

		Course course = courseRepository.findById(courseId)
				.orElseThrow(CourseNotFoundException::new);

		Assignment assignment = assignmentRepository.findByIdWithInstructor(assignmentId)
				.orElseThrow(AssignmentNotFoundException::new);

		Instructor instructor = instructorRepository.findById(user.getId())
				.orElseThrow(InstructorNotFoundException::new);

		if (!course.getInstructor().getId().equals(instructor.getId())) {
			throw new CourseInstructorMismatchException();
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
				.orElseThrow(AuthUserNotFoundException::new);

		Assignment assignment = assignmentRepository.findByIdWithInstructor(assignmentId)
				.orElseThrow(AssignmentNotFoundException::new);

		if (!assignment.getInstructor().getId().equals(user.getId())) {
			throw new AssignmentAccessDeniedException();
		}

		// 연관된 제출물의 S3 파일들 삭제
		List<Submission> submissions = submissionRepository.findByAssignmentId(assignmentId);
		for (Submission submission : submissions) {
			for (SubmissionFile submissionFile : submission.getFiles()) {
				try {
					s3FileService.deleteFile(submissionFile.getFileUrl());
				} catch (Exception e) {
					log.error("S3에서 제출물 파일 삭제를 실패했습니다: {}", submissionFile.getFileUrl(), e);
					throw new FileDeleteFailedException();
				}
			}
		}

		// 과제 S3 파일들 삭제
		for (AssignmentFile assignmentFile : assignment.getFiles()) {
			try {
				s3FileService.deleteFile(assignmentFile.getFileUrl());
			} catch (Exception e) {
				log.error("S3에서 파일 삭제를 실패했습니다.", assignmentFile.getFileUrl(), e);
				throw new FileDeleteFailedException();
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
				.orElseThrow(AssignmentNotFoundException::new);

		// 파일 확인
		AssignmentFile assignmentFile = assignment.getFiles().stream()
				.filter(f -> f.getId().equals(fileId))
				.findFirst()
				.orElseThrow(AssignmentFileNotFoundException::new);

		// 7일간 유효한 다운로드 링크
		return s3FileService.generatePresignedUrl(assignmentFile.getFileUrl(), Duration.ofDays(7));
	}

	/**
	 * 특정 파일 삭제
	 */
	@Transactional
	public AssignmentResponseDto deleteFile(Long courseId, Long assignmentId, Long fileId) {
		Assignment assignment = assignmentRepository.findByIdAndCourseId(assignmentId, courseId)
				.orElseThrow(AssignmentNotFoundException::new);

		AssignmentFile file = assignment.getFiles().stream()
				.filter(f -> f.getId().equals(fileId))
				.findFirst()
				.orElseThrow(AssignmentFileNotFoundException::new);

		try {
			s3FileService.deleteFile(file.getFileUrl());
		} catch (Exception e) {
			log.error("S3에서 파일 삭제를 실패했습니다: {}", file.getFileUrl(), e);
			throw new FileDeleteFailedException();
		}

		// 과제에서 파일 제거
		assignment.removeFile(file);

		return AssignmentResponseDto.from(assignment);
	}

	private String extractStoredFileName(String fileUrl) {
		return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
	}

}
