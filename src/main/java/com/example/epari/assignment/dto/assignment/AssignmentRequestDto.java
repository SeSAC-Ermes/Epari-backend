package com.example.epari.assignment.dto.assignment;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 과제 생성 및 수정을 위한 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class AssignmentRequestDto {

	private String title;           // 과제 제목

	private String description;     // 과제 설명

	private LocalDateTime deadline; // 마감기한

	private List<MultipartFile> files;


}
