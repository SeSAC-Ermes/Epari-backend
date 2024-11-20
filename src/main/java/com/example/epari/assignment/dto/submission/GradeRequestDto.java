package com.example.epari.assignment.dto.submission;

import com.example.epari.global.common.enums.SubmissionGrade;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GradeRequestDto {
	private SubmissionGrade grade;
	private String feedback;
}
