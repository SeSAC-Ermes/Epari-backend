package com.example.epari.assignment.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AssignmentRqDto {

	private String title;

	private String description;

	private LocalDateTime deadline;

	private Integer score;

}
