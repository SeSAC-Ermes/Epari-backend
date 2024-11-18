package com.example.epari.assignment.controller;

import com.example.epari.assignment.dto.assignment.AssignmentRequestDto;
import com.example.epari.assignment.dto.assignment.AssignmentResponseDto;
import com.example.epari.assignment.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
			@ModelAttribute AssignmentRequestDto requestDto) {
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
			@PathVariable Long courseId,
			@PathVariable Long assignmentId,
			@RequestParam Long instructorId,
			@ModelAttribute AssignmentRequestDto requestDto) {
		log.info("과제 수정 요청: ID = {}, 제목 = {}", assignmentId, requestDto.getTitle());
		AssignmentResponseDto responseDto = assignmentService.updateAssignment(courseId, assignmentId, requestDto, instructorId);
		log.info("과제 수정 완료: ID = {}", assignmentId);
		return ResponseEntity.ok(responseDto);
	}

	// 과제 삭제
	@DeleteMapping("/{assignmentId}")
	@PreAuthorize("hasRole('INSTRUCTOR')")
	public ResponseEntity<Void> deleteAssignment(
			@PathVariable Long courseId,
			@PathVariable Long assignmentId,
			@RequestParam Long instructorId) {
		log.info("과제 삭제 요청: ID = {}, instructorId = {}", assignmentId, instructorId);
		assignmentService.deleteAssignment(assignmentId, instructorId);
		log.info("과제 삭제 완료: ID = {}", assignmentId);
		return ResponseEntity.ok().build();
	}

}
