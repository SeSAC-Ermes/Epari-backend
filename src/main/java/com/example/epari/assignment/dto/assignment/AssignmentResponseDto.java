package com.example.epari.assignment.dto.assignment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.example.epari.assignment.domain.Assignment;
import com.example.epari.assignment.dto.file.AssignmentFileResponseDto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 과제 정보를 반환하기 위한 응답 DTO
 */
@Getter
@NoArgsConstructor
public class AssignmentResponseDto {

	private Long id;                // 과제 ID

	private String title;           // 과제 제목

	private String description;     // 과제 설명

	private LocalDateTime deadline; // 마감기한

	private String feedback;        // 전체 피드백

	private List<AssignmentFileResponseDto> files; // 첨부파일 목록

	public AssignmentResponseDto(Assignment assignment) {
		this.id = assignment.getId();
		this.title = assignment.getTitle();
		this.description = assignment.getDescription();
		this.deadline = assignment.getDeadline();
		this.feedback = assignment.getFeedback();
		this.files = assignment.getFiles().stream().map(AssignmentFileResponseDto::new).collect(Collectors.toList());
	}

}
