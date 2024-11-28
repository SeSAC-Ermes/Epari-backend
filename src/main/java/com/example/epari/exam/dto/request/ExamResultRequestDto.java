package com.example.epari.exam.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 시험 결과 조회 요청 데이터를 전달하기 위한 DTO 클래스 구현
 */
@Getter
@NoArgsConstructor
public class ExamResultRequestDto {

	private Long studentId;

	private Long courseId;

}
