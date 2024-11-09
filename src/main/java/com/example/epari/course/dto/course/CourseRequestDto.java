package com.example.epari.course.dto.course;

import java.time.LocalDate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 강의 생성/수정 요청용 DTO 클래스
 * 클라이언트로부터 강의 생성/수정 데이터를 전달받기 위한 객체입니다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CourseRequestDto {

	private String name;

	private LocalDate startDate;

	private LocalDate endDate;

	private String classroom;

}
