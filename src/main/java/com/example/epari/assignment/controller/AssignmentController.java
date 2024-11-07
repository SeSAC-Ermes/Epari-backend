package com.example.epari.assignment.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
@RequestMapping("/api/assignments")
@CrossOrigin(origins = "http://localhost:5173")
public class AssignmentController {

	private final AssignmentService assignmentService;

	// 과제 추가
	@PostMapping
	public ResponseEntity<AssignmentResponseDto> addAssignment(@RequestBody AssignmentRequestDto requestDto) {
		log.info("과제 생성 요청: 제목 = {}", requestDto.getTitle());
		try {
			AssignmentResponseDto responseDto = assignmentService.addAssignment(requestDto);
			log.info("과제 생성 완료: ID = {}", responseDto.getId());
			return ResponseEntity.ok(responseDto);
		} catch (Exception e) {
			log.error("과제 생성 실패: {}", e.getMessage(), e);
			throw e;
		}
	}

	// 전체 과제 조회
	@GetMapping
	public ResponseEntity<List<AssignmentResponseDto>> getAllAssignments() {
		log.info("전체 과제 목록 조회 요청");
		try {
			List<AssignmentResponseDto> assignments = assignmentService.getAllAssignments();
			log.info("과제 목록 조회 완료: {} 건", assignments.size());
			return ResponseEntity.ok(assignments);
		} catch (Exception e) {
			log.error("과제 목록 조회 실패: {}", e.getMessage(), e);
			throw e;
		}
	}

	// 입력 키워드를 포함하는 과제 조회
	@GetMapping("/search")
	public ResponseEntity<List<AssignmentResponseDto>> getAssignmentsByTitle(@RequestParam String title) {
		log.info("과제 검색 요청: 키워드 = {}", title);
		try {
			List<AssignmentResponseDto> assignments = assignmentService.getAssignmentsByTitle(title);
			log.info("과제 검색 완료: {} 건", assignments.size());
			return ResponseEntity.ok(assignments);
		} catch (Exception e) {
			log.error("과제 검색 실패: {}", e.getMessage(), e);
			throw e;
		}
	}

	// 과제 수정
	@PutMapping("/{id}")
	public ResponseEntity<AssignmentResponseDto> updateAssignment(
			@PathVariable Long id,
			@RequestBody AssignmentRequestDto requestDto) {
		log.info("과제 수정 요청: ID = {}, 제목 = {}", id, requestDto.getTitle());
		try {
			AssignmentResponseDto responseDto = assignmentService.updateAssignment(id, requestDto);
			log.info("과제 수정 완료: ID = {}", id);
			return ResponseEntity.ok(responseDto);
		} catch (Exception e) {
			log.error("과제 수정 실패: ID = {}, {}", id, e.getMessage(), e);
			throw e;
		}
	}

	// 과제 삭제
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
		log.info("과제 삭제 요청: ID = {}", id);
		try {
			assignmentService.deleteAssignment(id);
			log.info("과제 삭제 완료: ID = {}", id);
			return ResponseEntity.ok().build();
		} catch (Exception e) {
			log.error("과제 삭제 실패: ID = {}, {}", id, e.getMessage(), e);
			throw e;
		}
	}

	// 파일 업로드
	@PostMapping("/files")
	public ResponseEntity<List<AssignmentFileResponseDto>> uploadFiles(
			@RequestParam("files") List<MultipartFile> files,
			@RequestParam(required = false) Long assignmentId) {
		log.info("파일 업로드 요청: assignmentId = {}, 파일 개수 = {}", assignmentId, files.size());

		try {
			// 파일 정보 로깅
			files.forEach(file ->
					log.debug("업로드 파일 정보: 이름 = {}, 크기 = {}bytes, 타입 = {}",
							file.getOriginalFilename(),
							file.getSize(),
							file.getContentType())
			);

			List<AssignmentFileResponseDto> uploadedFiles = assignmentService.uploadFiles(files, assignmentId);
			log.info("파일 업로드 완료: {} 개 파일", uploadedFiles.size());
			return ResponseEntity.ok(uploadedFiles);
		} catch (Exception e) {
			log.error("파일 업로드 실패: assignmentId = {}, {}", assignmentId, e.getMessage(), e);
			throw e;
		}
	}

	// 파일 삭제
	@DeleteMapping("/files/{fileId}")
	public ResponseEntity<Void> deleteFile(@PathVariable Long fileId) {
		log.info("파일 삭제 요청: fileId = {}", fileId);
		try {
			assignmentService.deleteFile(fileId);
			log.info("파일 삭제 완료: fileId = {}", fileId);
			return ResponseEntity.ok().build();
		} catch (Exception e) {
			log.error("파일 삭제 실패: fileId = {}, {}", fileId, e.getMessage(), e);
			throw e;
		}
	}

}
