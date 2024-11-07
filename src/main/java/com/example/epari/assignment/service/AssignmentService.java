package com.example.epari.assignment.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.epari.assignment.domain.Assignment;
import com.example.epari.assignment.domain.AssignmentFile;
import com.example.epari.assignment.dto.assignment.AssignmentRequestDto;
import com.example.epari.assignment.dto.assignment.AssignmentResponseDto;
import com.example.epari.assignment.dto.file.AssignmentFileResponseDto;
import com.example.epari.assignment.repository.AssignmentFileRepository;
import com.example.epari.assignment.repository.AssignmentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

//로그를 간단하게 보여주는 어노테이션
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssignmentService {

	@Value("${file.upload.path}")  // application.properties에서 설정
	private String fileUploadPath;

	private final AssignmentRepository assignmentRepository;

	private final AssignmentFileRepository assignmentFileRepository;

	/**
	 * 과제 추가
	 */
	@Transactional
	public AssignmentResponseDto addAssignment(AssignmentRequestDto requestDto) {
		Assignment assignment = Assignment.createAssignment(requestDto.getTitle(), requestDto.getDescription(),
				requestDto.getDeadline(), null  // 강의 정보는 별도의 메서드로 처리
		);

		return new AssignmentResponseDto(assignmentRepository.save(assignment));
	}

	/**
	 * 전체 과제 조회
	 */
	public List<AssignmentResponseDto> getAllAssignments() {
		return assignmentRepository.findAll().stream().map(AssignmentResponseDto::new).collect(Collectors.toList());
	}

	/**
	 * 입력 키워드를 포함하는 과제 조회
	 */
	public List<AssignmentResponseDto> getAssignmentsByTitle(String title) {
		return assignmentRepository.findAssignmentByTitleContains(title)
				.stream()
				.map(AssignmentResponseDto::new)
				.collect(Collectors.toList());
	}

	/**
	 * 과제 수정
	 */
	@Transactional
	public AssignmentResponseDto updateAssignment(Long id, AssignmentRequestDto requestDto) {
		Assignment assignment = assignmentRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("과제를 찾을 수 없습니다."));

		assignment.updateAssignment(requestDto.getTitle(), requestDto.getDescription(), requestDto.getDeadline());

		return new AssignmentResponseDto(assignment);
	}

	/**
	 * 과제 삭제
	 */
	@Transactional
	public void deleteAssignment(Long id) {
		Assignment assignment = assignmentRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("과제를 찾을 수 없습니다."));
		assignmentRepository.delete(assignment);
	}

	@Transactional
	public List<AssignmentFileResponseDto> uploadFiles(List<MultipartFile> files, Long assignmentId) {
		// 과제 조회
		Assignment assignment = null;
		if (assignmentId != null) {
			assignment = assignmentRepository.findById(assignmentId)
					.orElseThrow(() -> new IllegalArgumentException("과제를 찾을 수 없습니다."));
		}

		List<AssignmentFile> uploadedFiles = new ArrayList<>();

		try {
			// 업로드 기본 디렉토리 생성
			Path uploadPath = Paths.get(fileUploadPath);
			if (!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
			}

			// 각 파일 처리
			for (MultipartFile file : files) {
				if (file.isEmpty())
					continue;

				// 파일명 생성 (UUID + 원본 파일명)
				String originalFileName = file.getOriginalFilename();
				String storedFileName = UUID.randomUUID().toString() + "_" + originalFileName;

				// 파일 저장 경로 생성
				Path filePath = uploadPath.resolve(storedFileName);

				// 파일 저장
				Files.copy(file.getInputStream(), filePath);

				// 파일 정보 엔티티 생성 (정적 팩토리 메서드 사용)
				AssignmentFile assignmentFile = AssignmentFile.createAssignmentFile(originalFileName, storedFileName,
						"/uploads/" + storedFileName, file.getSize(), assignment);

				// 파일 정보 저장
				AssignmentFile savedFile = assignmentFileRepository.save(assignmentFile);

				// 과제가 있는 경우 과제에 파일 추가
				if (assignment != null) {
					assignment.addFile(savedFile);
				}

				uploadedFiles.add(savedFile);
			}

			// DTO 변환 후 반환
			return uploadedFiles.stream().map(AssignmentFileResponseDto::new).collect(Collectors.toList());

		} catch (IOException e) {
			log.error("파일 업로드 중 오류 발생", e);
			throw new RuntimeException("파일 업로드에 실패했습니다: " + e.getMessage());
		}
	}

	/**
	 * 파일 삭제
	 */
	@Transactional
	public void deleteFile(Long fileId) {
		AssignmentFile file = assignmentFileRepository.findById(fileId)
				.orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다."));

		try {
			// 물리적 파일 삭제
			Path filePath = Paths.get(fileUploadPath).resolve(file.getStoredFileName());
			Files.deleteIfExists(filePath);

			// DB에서 파일 정보 삭제
			assignmentFileRepository.delete(file);

		} catch (IOException e) {
			log.error("파일 삭제 중 오류 발생", e);
			throw new RuntimeException("파일 삭제에 실패했습니다: " + e.getMessage());
		}
	}

}
