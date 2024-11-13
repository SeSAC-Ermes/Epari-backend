package com.example.epari.course.service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.epari.course.domain.Course;
import com.example.epari.course.domain.CourseContent;
import com.example.epari.course.domain.CourseContentFile;
import com.example.epari.course.dto.content.CourseContentRequestDto;
import com.example.epari.course.dto.content.CourseContentResponseDto;
import com.example.epari.course.repository.CourseContentRepository;
import com.example.epari.course.repository.CourseRepository;
import com.example.epari.global.common.service.S3FileService;

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
public class CourseContentService {

	private final CourseRepository courseRepository;

	private final CourseContentRepository courseContentRepository;

	private final S3FileService s3FileService;

	private static final String UPLOAD_DIR = "course-content";

	@Transactional
	public CourseContentResponseDto uploadContent(Long courseId, CourseContentRequestDto.Upload request) {
		Course course = courseRepository.findById(courseId)
				.orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

		CourseContent content = CourseContent.builder()
				.title(request.getTitle())
				.content(request.getContent())
				.date(request.getDate())
				.course(course)
				.build();

		// 파일 업로드 처리
		if (request.getFiles() != null && !request.getFiles().isEmpty()) {
			for (MultipartFile file : request.getFiles()) {
				String fileUrl = s3FileService.uploadFile(UPLOAD_DIR, file);

				CourseContentFile contentFile = CourseContentFile.createAttachment(
						file.getOriginalFilename(),
						extractStoredFileName(fileUrl),
						fileUrl,
						file.getSize(),
						content
				);

				content.addFile(contentFile);
			}
		}

		return CourseContentResponseDto.from(courseContentRepository.save(content));
	}

	public CourseContentResponseDto getContent(Long courseId, Long contentId) {
		CourseContent content = courseContentRepository.findByIdAndCourseId(contentId, courseId)
				.orElseThrow(() -> new IllegalArgumentException("강의 자료를 찾을 수 없습니다."));
		return CourseContentResponseDto.from(content);
	}

	public List<CourseContentResponseDto> getContents(Long courseId) {
		return courseContentRepository.findAllByCourseId(courseId).stream()
				.map(CourseContentResponseDto::from)
				.toList();
	}

	@Transactional
	public CourseContentResponseDto updateContent(Long courseId, Long contentId,
			CourseContentRequestDto.Update request) {
		CourseContent content = courseContentRepository.findByIdAndCourseId(contentId, courseId)
				.orElseThrow(() -> new IllegalArgumentException("강의 자료를 찾을 수 없습니다."));

		content.updateContent(request.getTitle(), request.getContent());

		// 새로운 파일이 있다면 업로드
		if (request.getFiles() != null && !request.getFiles().isEmpty()) {
			for (MultipartFile file : request.getFiles()) {
				String fileUrl = s3FileService.uploadFile(UPLOAD_DIR, file);

				CourseContentFile contentFile = CourseContentFile.createAttachment(
						file.getOriginalFilename(),
						extractStoredFileName(fileUrl),
						fileUrl,
						file.getSize(),
						content
				);

				content.addFile(contentFile);
			}
		}

		return CourseContentResponseDto.from(content);
	}

	@Transactional
	public void deleteContent(Long courseId, Long contentId) {
		CourseContent content = courseContentRepository.findByIdAndCourseId(contentId, courseId)
				.orElseThrow(() -> new IllegalArgumentException("강의 자료를 찾을 수 없습니다."));

		// S3에서 모든 첨부 파일 삭제
		for (CourseContentFile file : content.getFiles()) {
			try {
				s3FileService.deleteFile(file.getFileUrl());
			} catch (Exception e) {
				log.error("Failed to delete file from S3: {}", file.getFileUrl(), e);
			}
		}

		// DB에서 컨텐츠 삭제 (cascade로 인해 파일 엔티티도 함께 삭제됨)
		courseContentRepository.delete(content);
	}

	/**
	 * 중복 선택 삭제
	 */
	@Transactional
	public void deleteContents(Long courseId, List<Long> contentIds) {
		for (Long contentId : contentIds) {
			deleteContent(courseId, contentId);
		}
	}

	/**
	 * 다운로드
	 */
	public String downloadContent(Long courseId, Long contentId, Long fileId) {
		// 강의 자료 확인
		CourseContent content = courseContentRepository.findByIdAndCourseId(contentId, courseId)
				.orElseThrow(() -> new IllegalArgumentException("강의 자료를 찾을 수 없습니다."));

		// 파일 확인
		CourseContentFile file = content.getFiles().stream()
				.filter(f -> f.getId().equals(fileId))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다."));

		return s3FileService.generatePresignedUrl(file.getFileUrl(), Duration.ofDays(7));
	}

	/**
	 * 특정 파일 삭제
	 */
	@Transactional
	public CourseContentResponseDto deleteFile(Long courseId, Long contentId, Long fileId) {
		CourseContent content = courseContentRepository.findByIdAndCourseId(contentId, courseId)
				.orElseThrow(() -> new IllegalArgumentException("강의 자료를 찾을 수 없습니다."));

		CourseContentFile file = content.getFiles().stream()
				.filter(f -> f.getId().equals(fileId))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다."));

		// S3에서 파일 삭제
		try {
			s3FileService.deleteFile(file.getFileUrl());
		} catch (Exception e) {
			log.error("Failed to delete file from S3: {}", file.getFileUrl(), e);
		}

		// 컨텐츠에서 파일 제거
		content.removeFile(file);

		return CourseContentResponseDto.from(content);
	}

	/**
	 * 당일 날짜의 강의 자료를 조회합니다.
	 */
	public List<CourseContentResponseDto> getTodayContents(Long courseId) {
		LocalDate today = LocalDate.now();
		return courseContentRepository.findByCourseIdAndDate(courseId, today).stream()
				.map(CourseContentResponseDto::from)
				.toList();
	}

	private String extractStoredFileName(String fileUrl) {
		return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
	}

}
