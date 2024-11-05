package com.example.epari.lecture.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.epari.lecture.domain.Lecture;
import com.example.epari.lecture.dto.LectureResponseDto;
import com.example.epari.lecture.repository.CurriculumRepository;
import com.example.epari.lecture.repository.LectureRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LectureService {

	private final LectureRepository lectureRepository;
	private final CurriculumRepository curriculumRepository;

	public LectureResponseDto getLecture(Long lectureId) {
		// 강의 정보 조회 (강사 정보 포함)
		Lecture lecture = lectureRepository.findByIdWithIdInstructor(lectureId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의입니다. ID: " + lectureId));

		return LectureResponseDto.from(lecture);
	}
}
