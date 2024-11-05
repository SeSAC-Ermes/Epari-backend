package com.example.epari.assignment.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.epari.assignment.domain.Assignment;
import com.example.epari.assignment.service.AssignmentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/assign")
public class AssignmentController {

	private final AssignmentService assignmentService;

	// 과제 추가
	@PostMapping
	public Assignment addAssignment(@RequestBody Assignment assignment) {
		return assignmentService.addAssignment(assignment);
	}

	// 전체 과제 조회
	@GetMapping
	public List<Assignment> getAllAssignments() {
		return assignmentService.getAllAssignments();
	}

	// 입력 키워드를 포함하는 과제 조회
	@GetMapping("/search")
	public List<Assignment> getAssignmentsByTitle(@RequestParam String title) {
		return assignmentService.getAssignmentsByTitle(title);
	}

	// 과제 수정
	@PutMapping("/{id}")
	public Assignment updateAssignment(@PathVariable Long id,
			@RequestBody Assignment assignment) {
		return assignmentService.updateAssignment(id, assignment.getTitle(), assignment.getDescription(),
				assignment.getDeadline(), assignment.getScore());
	}

	// 과제 삭제
	@DeleteMapping("/{id}")
	public void deleteAssignment(@PathVariable Long id) {
		assignmentService.deleteAssignment(id);
	}

}
