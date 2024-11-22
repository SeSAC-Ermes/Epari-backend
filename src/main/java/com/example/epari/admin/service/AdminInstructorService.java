package com.example.epari.admin.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.epari.admin.dto.InstructorSearchResponseDTO;
import com.example.epari.admin.repository.AdminInstructorRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 관리자가 강사를 관리하는 비즈니스 로직을 처리하는 서비스 클래스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminInstructorService {

	private final AdminInstructorRepository instructorRepository;

	/**
	 * 이메일 기반 강사 검색 메서드
	 *
	 * @param email 검색할 이메일 (null 또는 빈 문자열이면 전체 검색)
	 * @return 검색된 강사 목록
	 */
	public List<InstructorSearchResponseDTO> searchInstructors(String email) {
		log.info("Searching instructors with email: {}", email);

		List<InstructorSearchResponseDTO> instructors = instructorRepository.searchInstructorsWithDTO(email);

		log.info("Found {} instructors", instructors.size());

		return instructors;
	}

}
