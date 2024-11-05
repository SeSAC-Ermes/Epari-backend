package com.example.epari.assignment.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.epari.assignment.domain.Assignment;
import com.example.epari.assignment.repository.AssignmentRepository;

import lombok.RequiredArgsConstructor;

/*
	과제 관련 서비스
 */

@Service
@RequiredArgsConstructor
public class AssignmentService {

	private final AssignmentRepository assignmentRepository;

	//과제 추가
	public Assignment addAssignment(Assignment assignment) {
		return assignmentRepository.save(assignment);
	}

	//전체 과제 조회
	public List<Assignment> getAllAssignments() {
		return assignmentRepository.findAll();
	}

	//입력 키워드를 포함하는 과제 조회
	public List<Assignment> getAssignmentsByTitle(String title) {
		return assignmentRepository.findAssignmentByTitleContains(title);
	}

	//과제 수정
	public Assignment updateAssignment(Long id, String title, String description, LocalDateTime deadline,
			Integer score) {
		Optional<Assignment> optionalAssignment = assignmentRepository.findById(id);

		if (optionalAssignment.isPresent()) {
			Assignment existingAssignment = optionalAssignment.get();
			existingAssignment.updateAssignment(title, description, deadline, score);
			return assignmentRepository.save(existingAssignment);
		} else {
			throw new IllegalArgumentException("과제를 찾을 수 없습니다.");
		}
	}

	//과제 삭제
	public void deleteAssignment(Long id) {
		assignmentRepository.deleteById(id);
	}

}
