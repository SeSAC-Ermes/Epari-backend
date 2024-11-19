package com.example.epari.exam.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

	@NotBlank(message = "시험 제목은 필수입니다.")
    private String title;
    
    @NotNull(message = "시험 일시는 필수입니다.")
    private LocalDateTime examDateTime;
    
    @Min(value = 1, message = "시험 시간은 1분 이상이어야 합니다.")
    private Integer duration;
    
    @Min(value = 100, message = "총점은 100점 이상이어야 합니다.")
    private Integer totalScore;

	private String description;

}
