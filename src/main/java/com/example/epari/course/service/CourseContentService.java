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
import com.example.epari.course.dto.content.CourseContentCursorDto;
import com.example.epari.course.dto.content.CourseContentListResponseDto;
import com.example.epari.course.dto.content.CourseContentRequestDto;
import com.example.epari.course.dto.content.CourseContentResponseDto;
import com.example.epari.course.dto.content.CourseContentSearchRequestDto;
import com.example.epari.course.repository.CourseContentRepository;
import com.example.epari.course.repository.CourseRepository;
import com.example.epari.global.common.service.S3FileService;
import com.example.epari.global.exception.course.CourseNotFoundException;
import com.example.epari.global.exception.file.CourseContentNotFoundException;
import com.example.epari.global.exception.file.CourseFileNotFoundException;
import com.example.epari.global.exception.file.FileDeleteFailedException;
import com.example.epari.global.exception.file.FileDownloadFailedException;
import com.example.epari.global.exception.file.FileUploadFailedException;

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

	private static final int PAGE_SIZE = 10;

	@Transactional
	public CourseContentResponseDto uploadContent(Long courseId, CourseContentRequestDto.Upload request) {
		log.info("Uploading content for course: {}", courseId);

		Course course = courseRepository.findById(courseId)
				.orElseThrow(CourseNotFoundException::new);

		CourseContent content = CourseContent.builder()
				.title(request.getTitle())
				.content(request.getContent())
				.date(request.getDate())
				.course(course)
				.build();

		// 파일 업로드 처리
		if (request.getFiles() != null && !request.getFiles().isEmpty()) {
			for (MultipartFile file : request.getFiles()) {
				try {
					String fileUrl = s3FileService.uploadFile(UPLOAD_DIR, file);
					CourseContentFile contentFile = CourseContentFile.createAttachment(
							file.getOriginalFilename(),
							extractStoredFileName(fileUrl),
							fileUrl,
							file.getSize(),
							content
					);
					content.addFile(contentFile);
				} catch (Exception e) {
					log.error("File upload failed for course: {}", courseId, e);
					throw new FileUploadFailedException();
				}
			}
		}

		CourseContent savedContent = courseContentRepository.save(content);
		log.info("Content uploaded successfully for course: {}", courseId);
		return CourseContentResponseDto.from(savedContent);
	}

	public CourseContentResponseDto getContent(Long courseId, Long contentId) {
		CourseContent content = courseContentRepository.findByIdAndCourseId(contentId, courseId)
				.orElseThrow(CourseNotFoundException::new);
		return CourseContentResponseDto.from(content);
	}

	/**
	 * 특정 강의에 모든 강의 자료 조회 (무한스크롤)
	 */
	public CourseContentListResponseDto getContents(Long courseId, CourseContentCursorDto cursor) {
		List<CourseContent> contents = courseContentRepository.findWithCursor(
				courseId,
				cursor,
				PAGE_SIZE + 1
		);

		List<CourseContentResponseDto> responseDtos = contents.stream()
				.map(CourseContentResponseDto::from)
				.toList();

		return CourseContentListResponseDto.of(responseDtos, PAGE_SIZE);
	}

	@Transactional
	public CourseContentResponseDto updateContent(Long courseId, Long contentId,
			CourseContentRequestDto.Update request) {
		log.info("Updating content: {} for course: {}", contentId, courseId);

		CourseContent content = courseContentRepository.findByIdAndCourseId(contentId, courseId)
				.orElseThrow(() -> new CourseContentNotFoundException());

		content.updateContent(request.getTitle(), request.getContent());

		if (request.getFiles() != null && !request.getFiles().isEmpty()) {
			for (MultipartFile file : request.getFiles()) {
				try {
					String fileUrl = s3FileService.uploadFile(UPLOAD_DIR, file);
					CourseContentFile contentFile = CourseContentFile.createAttachment(
							file.getOriginalFilename(),
							extractStoredFileName(fileUrl),
							fileUrl,
							file.getSize(),
							content
					);
					content.addFile(contentFile);
				} catch (Exception e) {
					log.error("File upload failed during content update - courseId: {}, contentId: {}",
							courseId, contentId, e);
					throw new FileUploadFailedException();
				}
			}
		}

		log.info("Content updated successfully - courseId: {}, contentId: {}", courseId, contentId);
		return CourseContentResponseDto.from(content);
	}

	@Transactional
	public void deleteContent(Long courseId, Long contentId) {
		log.info("Deleting content: {} from course: {}", contentId, courseId);

		CourseContent content = courseContentRepository.findByIdAndCourseId(contentId, courseId)
				.orElseThrow(CourseContentNotFoundException::new);

		for (CourseContentFile file : content.getFiles()) {
			try {
				s3FileService.deleteFile(file.getFileUrl());
			} catch (Exception e) {
				log.error("Failed to delete file from S3: {}", file.getFileUrl(), e);
				throw new FileDeleteFailedException();
			}
		}

		courseContentRepository.delete(content);
		log.info("Content deleted successfully - courseId: {}, contentId: {}", courseId, contentId);
	}

	/**
	 * 중복 선택 삭제
	 */
	@Transactional
	public void deleteContents(Long courseId, List<Long> contentIds) {
		log.info("Batch deleting contents: {} from course: {}", contentIds, courseId);
		contentIds.forEach(contentId -> deleteContent(courseId, contentId));
	}

	/**
	 * 다운로드
	 */
	public String downloadContent(Long courseId, Long contentId, Long fileId) {
		CourseContent content = courseContentRepository.findByIdAndCourseId(contentId, courseId)
				.orElseThrow(CourseContentNotFoundException::new);

		CourseContentFile file = content.getFiles().stream()
				.filter(f -> f.getId().equals(fileId))
				.findFirst()
				.orElseThrow(CourseFileNotFoundException::new);

		try {
			return s3FileService.generatePresignedUrl(file.getFileUrl(), Duration.ofDays(7));
		} catch (Exception e) {
			log.error("Failed to generate download URL - courseId: {}, contentId: {}, fileId: {}",
					courseId, contentId, fileId, e);
			throw new FileDownloadFailedException();
		}
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
	 * TODO: 단위 테스트 코드 여기로 작성해보기!!
	 */
	public List<CourseContentResponseDto> getTodayContents(Long courseId) {
		LocalDate today = LocalDate.now();
		return courseContentRepository.findByCourseIdAndDate(courseId, today).stream()
				.map(CourseContentResponseDto::from)
				.toList();
	}

	/**
	 * 검색 기능 (무한스크롤)
	 */
	public CourseContentListResponseDto searchContents(
			Long courseId,
			CourseContentSearchRequestDto searchRequest,
			CourseContentCursorDto cursor) {

		log.info("Searching course contents - courseId: {}, searchRequest: {}, cursor: {}",
				courseId, searchRequest, cursor);

		List<CourseContent> contents = courseContentRepository.searchWithCursor(
				courseId,
				searchRequest,
				cursor,
				PAGE_SIZE + 1
		);

		List<CourseContentResponseDto> responseDtos = contents.stream()
				.map(CourseContentResponseDto::from)
				.toList();

		return CourseContentListResponseDto.of(responseDtos, PAGE_SIZE);
	}

	private String extractStoredFileName(String fileUrl) {
		return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
	}

}
