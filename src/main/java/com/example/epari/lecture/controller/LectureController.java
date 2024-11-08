package com.example.epari.lecture.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.epari.global.annotation.CurrentUserEmail;
import com.example.epari.lecture.dto.lecture.LectureRequestDto;
import com.example.epari.lecture.dto.lecture.LectureResponseDto;
import com.example.epari.lecture.service.LectureService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/lectures")
@RequiredArgsConstructor
public class LectureController {

	private final LectureService lectureService;

	/**
	 * 강의 생성
	 * 강의 id로 조회
	 */
	@PostMapping
	@PreAuthorize("hasRole('INSTRUCTOR')")
	public ResponseEntity<LectureResponseDto> createLecture(
			@RequestParam Long instructorId,
			@RequestBody LectureRequestDto request) {
		return ResponseEntity.ok(lectureService.createLecture(instructorId, request));
	}

	/**
	 * 강의 id로 조회 상세 페이지 이동 시
	 */
	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('INSTRUCTOR', 'STUDENT')")
	public ResponseEntity<LectureResponseDto> getLecture(@PathVariable Long id) {
		return ResponseEntity.ok(lectureService.getLecture(id));
	}

	/**
	 * 사용자 역할에 따른 강의 목록 조회
	 */
	@GetMapping("/userlectures")
	@PreAuthorize("hasAnyRole('INSTRUCTOR', 'STUDENT')")
	public ResponseEntity<List<LectureResponseDto>> getMyLectures(
			@CurrentUserEmail String email) {

		List<LectureResponseDto> lectures = lectureService.getMyLectures(email);
		return ResponseEntity.ok(lectures);
	}

	/**
	 * 강의 id로 수정
	 * 강사 id로 검증
	 */
	@PutMapping("/{id}")
	@PreAuthorize("hasRole('INSTRUCTOR')")
	public ResponseEntity<LectureResponseDto> updateLecture(
			@PathVariable Long id,
			@RequestParam Long instructorId,
			@RequestBody LectureRequestDto request
	) {
		return ResponseEntity.ok(lectureService.updateLecture(id, instructorId, request));
	}

	/**
	 * 강의 id로 삭제
	 * 강사 id로 검증
	 */
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('INSTRUCTOR')")
	public ResponseEntity<Void> deleteLecture(
			@PathVariable Long id,
			@RequestParam Long instructorId
	) {
		lectureService.deleteLecture(id, instructorId);
		return ResponseEntity.ok().build();
	}

}
