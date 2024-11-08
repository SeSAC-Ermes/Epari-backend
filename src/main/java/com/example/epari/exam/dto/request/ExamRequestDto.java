package com.example.epari.exam.dto.request;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 시험 생성 요청을 위한 DTO 클래스
 */
@Getter
@Setter
@NoArgsConstructor
public class ExamRequestDto {

	private String title;

	private LocalDateTime examDateTime;

	private Integer duration;

	private Integer totalScore;

	private String description;

}
