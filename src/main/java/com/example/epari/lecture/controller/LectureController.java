package com.example.epari.lecture.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.epari.lecture.dto.LectureResponseDto;
import com.example.epari.lecture.service.LectureService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/lectures")
@RequiredArgsConstructor
public class LectureController {

	private final LectureService lectureService;

	@GetMapping("/{id}")
	public ResponseEntity<LectureResponseDto> getLecture(@PathVariable Long id) {
		return ResponseEntity.ok(lectureService.getLecture(id));
	}
}
