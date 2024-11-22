package com.example.epari.assignment.service;

import com.example.epari.assignment.domain.Assignment;
import com.example.epari.assignment.domain.Submission;
import com.example.epari.assignment.domain.SubmissionFile;
import com.example.epari.assignment.dto.submission.SubmissionRequestDto;
import com.example.epari.assignment.dto.submission.SubmissionResponseDto;
import com.example.epari.assignment.repository.AssignmentRepository;
import com.example.epari.assignment.repository.SubmissionRepository;
import com.example.epari.course.domain.Course;
import com.example.epari.course.repository.CourseRepository;
import com.example.epari.global.common.enums.SubmissionGrade;
import com.example.epari.global.common.service.S3FileService;
import com.example.epari.user.domain.Student;
import com.example.epari.user.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

		// 기존 제출물이 있는지 확인
		Optional<Submission> existingSubmission = submissionRepository
				.findByAssignmentIdAndStudentId(assignmentId, studentId);

		Submission submission;
		if (existingSubmission.isPresent()) {
			// 기존 제출물이 있으면 업데이트
			submission = existingSubmission.get();
			submission.updateSubmission(requestDto.getDescription());

			// 새로운 파일 추가 전에 기존 파일 유지
		} else {
			// 새로운 제출물 생성
			submission = Submission.createSubmission(requestDto.getDescription(), assignment, student);
		}

		// 파일 업로드 처리
		if (requestDto.getFiles() != null && !requestDto.getFiles().isEmpty()) {
			for (MultipartFile file : requestDto.getFiles()) {
				String fileUrl = s3FileService.uploadFile("submissions", file);

				SubmissionFile submissionFile = SubmissionFile.createSubmissionFile(
						file.getOriginalFilename(),
						extractStoredFileName(fileUrl),
						fileUrl,
						file.getSize(),
						submission
				);

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
	 * 학생의 과제 제출물 조회
	 */
	public SubmissionResponseDto getStudentSubmission(Long courseId, Long assignmentId, Long studentId) {
		Optional<Submission> submission = submissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId);

		// 제출물이 없으면 null 반환 (404 대신)
		if (submission.isEmpty()) {
			return null;
		}

		Submission foundSubmission = submission.get();
		// 제출된 과제가 해당 코스에 속하는지 확인
		if (!foundSubmission.getAssignment().getCourse().getId().equals(courseId)) {
			throw new IllegalArgumentException("해당 과제의 제출물이 아닙니다.");
		}

		return SubmissionResponseDto.from(foundSubmission);
	}

	/**
	 * 전체 과제 조회
	 */
	public List<SubmissionResponseDto> getSubmissionsByAssignmentId(Long assignmentId) {
		List<Submission> submissions = submissionRepository.findByAssignmentId(assignmentId);
		return submissions.stream()
				.map(SubmissionResponseDto::from)
				.collect(Collectors.toList());
	}

	/**
	 * 특정 강의의 모든 과제 제출물 조회
	 */
	public List<SubmissionResponseDto> getSubmissionsByCourse(Long courseId) {
		// 강의 존재 여부 확인
		Course course = courseRepository.findById(courseId)
				.orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

		List<Submission> submissions = submissionRepository.findByCourseId(courseId);
		return submissions.stream()
				.map(SubmissionResponseDto::from)
				.collect(Collectors.toList());
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
	public SubmissionResponseDto gradeSubmission(Long submissionId, SubmissionGrade grade, String feedback) {
		Submission submission = submissionRepository.findById(submissionId)
				.orElseThrow(() -> new IllegalArgumentException("제출된 과제를 찾을 수 없습니다."));

		submission.updateGrade(grade, feedback);

		return SubmissionResponseDto.from(submission);
	}

	/**
	 * 과제 제출물 삭제
	 */
	@Transactional
	public void deleteSubmission(Long courseId, Long assignmentId, Long submissionId, Long studentId) {
		Submission submission = submissionRepository.findById(submissionId)
				.orElseThrow(() -> new IllegalArgumentException("제출된 과제를 찾을 수 없습니다."));

		// 삭제 권한 검증
		if (!submission.getStudent().getId().equals(studentId)) {
			throw new IllegalArgumentException("해당 과제의 삭제 권한이 없습니다.");
		}

		// 과제가 해당 코스와 과제에 속하는지 확인
		if (!submission.getAssignment().getId().equals(assignmentId) ||
				!submission.getAssignment().getCourse().getId().equals(courseId)) {
			throw new IllegalArgumentException("해당 과제의 제출물이 아닙니다.");
		}

		// S3에서 관련 파일들 삭제
		for (SubmissionFile submissionFile : submission.getFiles()) {
			try {
				s3FileService.deleteFile(submissionFile.getFileUrl());
			} catch (Exception e) {
				log.error("S3에서 파일 삭제를 실패했습니다: {}", submissionFile.getFileUrl(), e);
			}
		}

		submissionRepository.delete(submission);
	}

	/**
	 * 전체 학생 조회
	 */
	public List<SubmissionResponseDto> getSubmissionsWithStudents(Long courseId, Long assignmentId) {
		// 과제 정보 조회
		Assignment assignment = assignmentRepository.findById(assignmentId)
				.orElseThrow(() -> new IllegalArgumentException("과제를 찾을 수 없습니다."));

		// 과제가 해당 코스의 것인지 확인
		if (!assignment.getCourse().getId().equals(courseId)) {
			throw new IllegalArgumentException("해당 코스의 과제가 아닙니다.");
		}

		// 제출된 과제 목록 조회
		List<Submission> submissions = submissionRepository
				.findByAssignmentIdAndCourseIdWithStudent(assignmentId, courseId);

		// 해당 코스의 모든 학생 목록 조회
		List<Student> enrolledStudents = studentRepository.findByCourseId(courseId);

		// 제출/미제출 학생 모두 포함한 응답 DTO 생성
		return enrolledStudents.stream()
				.map(student -> {
					Optional<Submission> studentSubmission = submissions.stream()
							.filter(sub -> sub.getStudent().getId().equals(student.getId()))
							.findFirst();

					return studentSubmission
							.map(SubmissionResponseDto::from)
							.orElse(SubmissionResponseDto.createUnsubmitted(student, assignment));
				})
				.collect(Collectors.toList());
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
