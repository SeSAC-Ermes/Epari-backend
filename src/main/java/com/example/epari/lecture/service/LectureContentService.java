package com.example.epari.lecture.service;

import java.time.Duration;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.epari.global.common.service.S3FileService;
import com.example.epari.lecture.domain.Lecture;
import com.example.epari.lecture.domain.LectureContent;
import com.example.epari.lecture.domain.LectureContentFile;
import com.example.epari.lecture.dto.content.LectureContentRequestDto;
import com.example.epari.lecture.dto.content.LectureContentResponseDto;
import com.example.epari.lecture.repository.LectureContentRepository;
import com.example.epari.lecture.repository.LectureRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 강의 컨텐츠 관련 비즈니스 로직을 처리하는 서비스
 * 강의 자료의 CRUD 및 파일 업로드/다운로드를 처리합니다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class LectureContentService {

	private final LectureRepository lectureRepository;

	private final LectureContentRepository lectureContentRepository;

	private final S3FileService s3FileService;

	private static final String UPLOAD_DIR = "lecture-content";

	@Transactional
	public LectureContentResponseDto uploadContent(Long lectureId, LectureContentRequestDto.Upload request) {
		Lecture lecture = lectureRepository.findById(lectureId)
				.orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

		LectureContent content = LectureContent.builder()
				.title(request.getTitle())
				.content(request.getContent())
				.date(request.getDate())
				.lecture(lecture)
				.build();

		// 파일 업로드 처리
		if (request.getFiles() != null && !request.getFiles().isEmpty()) {
			for (MultipartFile file : request.getFiles()) {
				String fileUrl = s3FileService.uploadFile(UPLOAD_DIR, file);

				LectureContentFile contentFile = LectureContentFile.createAttachment(
						file.getOriginalFilename(),
						extractStoredFileName(fileUrl),
						fileUrl,
						file.getSize(),
						content
				);

				content.addFile(contentFile);
			}
		}

		return LectureContentResponseDto.from(lectureContentRepository.save(content));
	}

	public LectureContentResponseDto getContent(Long lectureId, Long contentId) {
		LectureContent content = lectureContentRepository.findByIdAndLectureId(contentId, lectureId)
				.orElseThrow(() -> new IllegalArgumentException("강의 자료를 찾을 수 없습니다."));
		return LectureContentResponseDto.from(content);
	}

	public List<LectureContentResponseDto> getContents(Long lectureId) {
		return lectureContentRepository.findAllByLectureId(lectureId).stream()
				.map(LectureContentResponseDto::from)
				.toList();
	}

	@Transactional
	public LectureContentResponseDto updateContent(Long lectureId, Long contentId,
			LectureContentRequestDto.Update request) {
		LectureContent content = lectureContentRepository.findByIdAndLectureId(contentId, lectureId)
				.orElseThrow(() -> new IllegalArgumentException("강의 자료를 찾을 수 없습니다."));

		content.updateContent(request.getTitle(), request.getContent());
		return LectureContentResponseDto.from(content);
	}

	@Transactional
	public void deleteContent(Long lectureId, Long contentId) {
		LectureContent content = lectureContentRepository.findByIdAndLectureId(contentId, lectureId)
				.orElseThrow(() -> new IllegalArgumentException("강의 자료를 찾을 수 없습니다."));

		// S3에서 모든 첨부 파일 삭제
		for (LectureContentFile file : content.getFiles()) {
			try {
				s3FileService.deleteFile(file.getFileUrl());
			} catch (Exception e) {
				log.error("Failed to delete file from S3: {}", file.getFileUrl(), e);
			}
		}

		// DB에서 컨텐츠 삭제 (cascade로 인해 파일 엔티티도 함께 삭제됨)
		lectureContentRepository.delete(content);
	}

	/**
	 * 중복 선택 삭제
	 */
	@Transactional
	public void deleteContents(Long lectureId, List<Long> contentIds) {
		for (Long contentId : contentIds) {
			deleteContent(lectureId, contentId);
		}
	}

	/**
	 * 다운로드
	 */
	public String downloadContent(Long lectureId, Long contentId, Long fileId) {
		// 강의 자료 확인
		LectureContent content = lectureContentRepository.findByIdAndLectureId(contentId, lectureId)
				.orElseThrow(() -> new IllegalArgumentException("강의 자료를 찾을 수 없습니다."));

		// 파일 확인
		LectureContentFile file = content.getFiles().stream()
				.filter(f -> f.getId().equals(fileId))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다."));

		return s3FileService.generatePresignedUrl(file.getFileUrl(), Duration.ofDays(7));
	}

	private String extractStoredFileName(String fileUrl) {
		return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
	}

}
