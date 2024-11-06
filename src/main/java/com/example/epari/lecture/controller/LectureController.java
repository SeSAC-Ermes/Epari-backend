package com.example.epari.lecture.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.epari.lecture.dto.lecture.LectureRequestDto;
import com.example.epari.lecture.dto.lecture.LectureResponseDto;
import com.example.epari.lecture.service.LectureService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/lectures")
@RequiredArgsConstructor
public class LectureController {

	private final LectureService lectureService;

	// 생성
	@PostMapping
	public ResponseEntity<LectureResponseDto> createLecture(
			@RequestParam Long instructorId,
			@RequestBody LectureRequestDto request) {
		return ResponseEntity.ok(lectureService.createLecture(instructorId, request));
	}

	// 조회
	@GetMapping("/{id}")
	public ResponseEntity<LectureResponseDto> getLecture(@PathVariable Long id) {
		return ResponseEntity.ok(lectureService.getLecture(id));
	}

	// 수정
	@PutMapping("/{id}")
	public ResponseEntity<LectureResponseDto> updateLecture(
			@PathVariable Long id,
			@RequestParam Long instructorId,
			@RequestBody LectureRequestDto request
	) {
		return ResponseEntity.ok(lectureService.updateLecture(id, instructorId, request));
	}

	// 삭제
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteLecture(
			@PathVariable Long id,
			@RequestParam Long instructorId
	) {
		lectureService.deleteLecture(id, instructorId);
		return ResponseEntity.ok().build();
	}

}
