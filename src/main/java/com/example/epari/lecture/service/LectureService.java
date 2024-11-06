package com.example.epari.lecture.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.epari.lecture.domain.Lecture;
import com.example.epari.lecture.dto.lecture.LectureRequestDto;
import com.example.epari.lecture.dto.lecture.LectureResponseDto;
import com.example.epari.lecture.repository.LectureRepository;
import com.example.epari.user.domain.Instructor;
import com.example.epari.user.repository.InstructorRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LectureService {

	private final LectureRepository lectureRepository;

	private final InstructorRepository instructorRepository;

	// 강의 생성
	@Transactional
	public LectureResponseDto createLecture(Long instructorId, LectureRequestDto request) {
		Instructor instructor = instructorRepository.findById(instructorId)
				.orElseThrow(() -> new IllegalArgumentException("강사를 찾을 수 없습니다. ID: " + instructorId));

		Lecture lecture = Lecture.createLecture(
				request.getName(),
				request.getStartDate(),
				request.getEndDate(),
				request.getClassroom(),
				instructor
		);
		return LectureResponseDto.from(lectureRepository.save(lecture));
	}

	// 강의 조회
	public LectureResponseDto getLecture(Long lectureId) {
		Lecture lecture = lectureRepository.findById(lectureId)
				.orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다. ID: " + lectureId));
		return LectureResponseDto.from(lecture);
	}

	// 강의 수정
	@Transactional
	public LectureResponseDto updateLecture(Long lectureId, Long instructorId, LectureRequestDto request) {
		Lecture lecture = lectureRepository.findById(lectureId)
				.orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다. ID: " + lectureId));

		if (!lecture.getInstructor().getId().equals(instructorId)) {
			throw new IllegalArgumentException("해당 강의에 대한 권한이 없습니다.");
		}

		lecture.updateLecture(
				request.getName(),
				request.getStartDate(),
				request.getEndDate(),
				request.getClassroom()
		);
		return LectureResponseDto.from(lecture);
	}

	// 강의 삭제
	@Transactional
	public void deleteLecture(Long lectureId, Long instructorId) {
		Lecture lecture = lectureRepository.findById(lectureId)
				.orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다. ID: " + lectureId));

		if (!lecture.getInstructor().getId().equals(instructorId)) {
			throw new IllegalArgumentException("해당 강의에 대한 권한이 없습니다.");
		}

		lectureRepository.deleteById(lectureId);
	}

}
