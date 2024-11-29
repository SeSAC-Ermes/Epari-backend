package com.example.epari.course.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.epari.course.service.CsvImportService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class DataImportController {

	private final CsvImportService csvImportService;

	@PostMapping("/courses/{courseId}/import")
	public ResponseEntity<Void> importCourseContent(
			@PathVariable Long courseId,
			@RequestParam String filename) {
		log.info("Starting CSV import for course: {}, filename: {}", courseId, filename);
		csvImportService.importCsvContent(courseId, filename);
		return ResponseEntity.ok().build();
	}
}
