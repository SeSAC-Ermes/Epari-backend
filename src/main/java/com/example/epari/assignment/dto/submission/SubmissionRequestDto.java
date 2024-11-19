package com.example.epari.assignment.dto.submission;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 과제 제출 및 수정을 위한 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class SubmissionRequestDto {

	private String description;

	private List<MultipartFile> files;

}
