package com.example.epari.assignment.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.epari.assignment.dto.assignment.AssignmentRequestDto;
import com.example.epari.assignment.dto.assignment.AssignmentResponseDto;
import com.example.epari.assignment.dto.file.AssignmentFileResponseDto;
import com.example.epari.assignment.service.AssignmentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/courses/{courseId}/assignments")
public class AssignmentController {

	private final AssignmentService assignmentService;

	// 과제 추가
	@PostMapping
	@PreAuthorize("hasRole('INSTRUCTOR')")
	public ResponseEntity<AssignmentResponseDto> addAssignment(
			@PathVariable Long courseId,
			@RequestParam Long instructorId,
			@RequestBody AssignmentRequestDto requestDto) {
		log.info("과제 생성 요청: courseId = {}, 제목 = {}, instructorId = {}",
				courseId, requestDto.getTitle(), instructorId);
		AssignmentResponseDto responseDto = assignmentService.addAssignment(courseId, requestDto, instructorId);
		log.info("과제 생성 완료: ID = {}", responseDto.getId());
		return ResponseEntity.ok(responseDto);
	}

	// 전체 과제 조회
	@GetMapping
	public ResponseEntity<List<AssignmentResponseDto>> getAssignmentsByCourse(@PathVariable Long courseId) {
		log.info("강의 과제 목록 조회 요청: courseId = {}", courseId);
		List<AssignmentResponseDto> assignments = assignmentService.getAssignmentsByCourse(courseId);
		log.info("과제 목록 조회 완료: {} 건", assignments.size());
		return ResponseEntity.ok(assignments);
	}

	// 과제 상세 조회
	@GetMapping("/{assignmentId}")
	public ResponseEntity<AssignmentResponseDto> getAssignmentById(
			@PathVariable Long courseId,
			@PathVariable Long assignmentId) {
		AssignmentResponseDto responseDto = assignmentService.getAssignmentById(courseId, assignmentId);
		return ResponseEntity.ok(responseDto);
	}

	// 과제 수정
	@PutMapping("/{assignmentId}")
	@PreAuthorize("hasRole('INSTRUCTOR')")
	public ResponseEntity<AssignmentResponseDto> updateAssignment(
			@PathVariable Long assignmentId,
			@RequestParam Long instructorId,
			@RequestBody AssignmentRequestDto requestDto) {
		log.info("과제 수정 요청: ID = {}, 제목 = {}", assignmentId, requestDto.getTitle());
		AssignmentResponseDto responseDto = assignmentService.updateAssignment(assignmentId, requestDto, instructorId);
		log.info("과제 수정 완료: ID = {}", assignmentId);
		return ResponseEntity.ok(responseDto);
	}

	// 과제 삭제
	@DeleteMapping("/{assignmentId}")
	@PreAuthorize("hasRole('INSTRUCTOR')")
	public ResponseEntity<Void> deleteAssignment(
			@PathVariable Long assignmentId,
			@RequestParam Long instructorId) {
		log.info("과제 삭제 요청: ID = {}, instructorId = {}", assignmentId, instructorId);
		assignmentService.deleteAssignment(assignmentId, instructorId);
		log.info("과제 삭제 완료: ID = {}", assignmentId);
		return ResponseEntity.ok().build();
	}

	// 파일 업로드
	@PostMapping("/files")
	public ResponseEntity<List<AssignmentFileResponseDto>> uploadFiles(
			@RequestParam("files") List<MultipartFile> files,
			@RequestParam(required = false) Long assignmentId) {
		log.info("파일 업로드 요청: assignmentId = {}, 파일 개수 = {}", assignmentId, files.size());
		files.forEach(file -> log.debug("업로드 파일 정보: 이름 = {}, 크기 = {}bytes, 타입 = {}",
				file.getOriginalFilename(),
				file.getSize(),
				file.getContentType()));

		List<AssignmentFileResponseDto> uploadedFiles = assignmentService.uploadFiles(files, assignmentId);
		log.info("파일 업로드 완료: {} 개 파일", uploadedFiles.size());
		return ResponseEntity.ok(uploadedFiles);
	}

	// 파일 삭제
	@DeleteMapping("/files/{fileId}")
	public ResponseEntity<Void> deleteFile(@PathVariable Long fileId) {
		log.info("파일 삭제 요청: fileId = {}", fileId);
		assignmentService.deleteFile(fileId);
		log.info("파일 삭제 완료: fileId = {}", fileId);
		return ResponseEntity.ok().build();
	}

}
