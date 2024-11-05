package com.example.epari.lecture.dto.lecture;

import java.time.LocalDate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LectureRequestDto {

	private String name;

	private LocalDate startDate;

	private LocalDate endDate;

	private String classroom;

}
