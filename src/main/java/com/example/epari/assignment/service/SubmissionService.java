package com.example.epari.assignment.service;

import java.time.Duration;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.epari.assignment.domain.Assignment;
import com.example.epari.assignment.domain.Submission;
import com.example.epari.assignment.domain.SubmissionFile;
import com.example.epari.assignment.dto.submission.SubmissionRequestDto;
import com.example.epari.assignment.dto.submission.SubmissionResponseDto;
import com.example.epari.assignment.repository.AssignmentRepository;
import com.example.epari.assignment.repository.SubmissionRepository;
import com.example.epari.course.domain.Course;
import com.example.epari.course.repository.CourseRepository;
import com.example.epari.global.common.service.S3FileService;
import com.example.epari.user.domain.Student;
import com.example.epari.user.repository.StudentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubmissionService {

	private final SubmissionRepository submissionRepository;

	private final CourseRepository courseRepository;

	private final AssignmentRepository assignmentRepository;

	private final StudentRepository studentRepository;

	private final S3FileService s3FileService;

	/**
	 * 과제 제출
	 */
	@Transactional
	public SubmissionResponseDto addSubmission(Long courseId, Long assignmentId, SubmissionRequestDto requestDto,
			Long studentId) {
		Course course = courseRepository.findById(courseId)
				.orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

		Assignment assignment = assignmentRepository.findById(assignmentId)
				.orElseThrow(() -> new IllegalArgumentException("과제를 찾을 수 없습니다."));

		Student student = studentRepository.findById(studentId)
				.orElseThrow(() -> new IllegalArgumentException("학생 정보를 찾을 수 없습니다."));

		Submission submission = Submission.createSubmission(requestDto.getDescription(), assignment, student);

		// 파일 업로드 처리
		if (requestDto.getFiles() != null && !requestDto.getFiles().isEmpty()) {
			for (MultipartFile file : requestDto.getFiles()) {
				String fileUrl = s3FileService.uploadFile("submissions", file);

				SubmissionFile submissionFile = SubmissionFile.createSubmissionFile(file.getOriginalFilename(),
						extractStoredFileName(fileUrl), fileUrl, file.getSize(), submission);

				submission.addFile(submissionFile);
			}
		}

		return SubmissionResponseDto.from(submissionRepository.save(submission));
	}

	/**
	 * 과제 제출물 조회
	 */
	public SubmissionResponseDto getSubmissionById(Long courseId, Long assignmentId, Long submissionId) {
		Submission submission = submissionRepository.findById(submissionId)
				.orElseThrow(() -> new IllegalArgumentException("제출된 과제를 찾을 수 없습니다."));

		// 제출된 과제가 해당 코스와 과제에 속하는지 확인
		if (!submission.getAssignment().getId().equals(assignmentId) || !submission.getAssignment()
				.getCourse()
				.getId()
				.equals(courseId)) {
			throw new IllegalArgumentException("해당 과제의 제출물이 아닙니다.");
		}

		return SubmissionResponseDto.from(submission);
	}

	/**
	 * 과제 제출물 수정
	 */
	@Transactional
	public SubmissionResponseDto updateSubmission(Long courseId, Long assignmentId, Long submissionId,
			SubmissionRequestDto requestDto, Long studentId) {
		Submission submission = submissionRepository.findById(submissionId)
				.orElseThrow(() -> new IllegalArgumentException("제출된 과제를 찾을 수 없습니다."));

		// 수정 권한 검증
		if (!submission.getStudent().getId().equals(studentId)) {
			throw new IllegalArgumentException("해당 과제의 수정 권한이 없습니다.");
		}

		submission.updateSubmission(requestDto.getDescription());

		// 파일 업로드 처리
		if (requestDto.getFiles() != null && !requestDto.getFiles().isEmpty()) {
			for (MultipartFile file : requestDto.getFiles()) {
				String fileUrl = s3FileService.uploadFile("submissions", file);

				SubmissionFile submissionFile = SubmissionFile.createSubmissionFile(file.getOriginalFilename(),
						extractStoredFileName(fileUrl), fileUrl, file.getSize(), submission);

				submission.addFile(submissionFile);
			}
		}

		return SubmissionResponseDto.from(submission);
	}

	/**
	 * 과제 채점
	 */
	@Transactional
	public SubmissionResponseDto gradeSubmission(Long submissionId, String grade, String feedback) {
		Submission submission = submissionRepository.findById(submissionId)
				.orElseThrow(() -> new IllegalArgumentException("제출된 과제를 찾을 수 없습니다."));

		submission.updateGrade(grade, feedback);

		return SubmissionResponseDto.from(submission);
	}

	/**
	 * 파일 다운로드
	 */
	public String downloadFile(Long courseId, Long assignmentId, Long submissionId, Long fileId) {
		Submission submission = submissionRepository.findById(submissionId)
				.orElseThrow(() -> new IllegalArgumentException("제출된 과제를 찾을 수 없습니다."));

		SubmissionFile submissionFile = submission.getFiles()
				.stream()
				.filter(f -> f.getId().equals(fileId))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다."));

		return s3FileService.generatePresignedUrl(submissionFile.getFileUrl(), Duration.ofDays(7));
	}

	/**
	 * 특정 파일 삭제
	 */
	@Transactional
	public SubmissionResponseDto deleteFile(Long courseId, Long assignmentId, Long submissionId, Long fileId) {
		Submission submission = submissionRepository.findById(submissionId)
				.orElseThrow(() -> new IllegalArgumentException("제출된 과제를 찾을 수 없습니다."));

		SubmissionFile file = submission.getFiles().stream()
				.filter(f -> f.getId().equals(fileId))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다."));

		try {
			s3FileService.deleteFile(file.getFileUrl());
		} catch (Exception e) {
			log.error("S3에서 파일 삭제를 실패했습니다: {}", file.getFileUrl(), e);
		}

		submission.removeFile(file);

		return SubmissionResponseDto.from(submission);
	}

	private String extractStoredFileName(String fileUrl) {
		return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
	}

}
