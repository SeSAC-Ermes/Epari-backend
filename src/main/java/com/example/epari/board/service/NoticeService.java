package com.example.epari.board.service;

import com.example.epari.board.domain.Notice;
import com.example.epari.board.domain.NoticeFile;
import com.example.epari.board.dto.NoticeRequestDto;
import com.example.epari.board.dto.NoticeResponseDto;
import com.example.epari.board.repository.NoticeFileRepository;
import com.example.epari.board.repository.NoticeRepository;
import com.example.epari.course.domain.Course;
import com.example.epari.course.repository.CourseRepository;
import com.example.epari.global.common.enums.NoticeType;
import com.example.epari.global.common.service.S3FileService;
import com.example.epari.global.config.aws.AwsS3Properties;
import com.example.epari.user.domain.Instructor;
import com.example.epari.user.repository.InstructorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NoticeService {

	private final NoticeRepository noticeRepository;

	private final NoticeFileRepository noticeFileRepository;

	private final CourseRepository courseRepository;

	private final InstructorRepository instructorRepository;

	private final S3FileService s3FileService;

	private final AwsS3Properties awsS3Properties;

	// 공지사항 작성
	@Transactional
	public Long createNotice(NoticeRequestDto requestDto) {
		try {
			Course course = courseRepository.findById(requestDto.getCourseId())
					.orElseThrow(() -> new EntityNotFoundException("Course not found"));

			Instructor instructor = instructorRepository.findById(requestDto.getInstructorId())
					.orElseThrow(() -> new EntityNotFoundException("Instructor not found"));

			Notice notice = noticeRepository.save(Notice.builder()
					.title(requestDto.getTitle())
					.content(requestDto.getContent())
					.type(requestDto.getType())
					.course(course)
					.instructor(instructor)
					.build());

			// S3에 파일 업로드 및 NoticeFile 엔티티 생성
			if (requestDto.getFiles() != null && !requestDto.getFiles().isEmpty()) {
				for (MultipartFile file : requestDto.getFiles()) {
					// S3에 파일 업로드
					String s3Key = generateS3Key(file.getOriginalFilename());
					String fileUrl = s3FileService.uploadFile("notices/files", file);

					// NoticeFile 엔티티 생성 및 저장
					NoticeFile noticeFile = NoticeFile.createNoticeFile(
							file.getOriginalFilename(),
							s3Key,
							fileUrl,
							file.getSize(),
							notice
					);
					noticeFileRepository.save(noticeFile);
				}
			}

			return notice.getId();
		} catch (Exception e) {
			log.error("Error creating notice", e);
			throw new RuntimeException("공지사항 생성 중 오류가 발생했습니다.", e);
		}
	}

//	@Transactional
//	public Long createNotice(NoticeRequestDto requestDto) {
//		try {
//			Course course = courseRepository.findById(requestDto.getCourseId())
//					.orElseThrow(() -> new EntityNotFoundException("Course not found"));
//
//			Instructor instructor = instructorRepository.findById(requestDto.getInstructorId())
//					.orElseThrow(() -> new EntityNotFoundException("Instructor not found"));
//
//			Notice notice = noticeRepository.save(Notice.builder()
//					.title(requestDto.getTitle())
//					.content(requestDto.getContent())
//					.type(requestDto.getType())
//					.course(course)
//					.instructor(instructor)
//					.build());
//
//			// S3에 파일 업로드
//			if (requestDto.getFiles() != null && !requestDto.getFiles().isEmpty()) {
//				for (MultipartFile file : requestDto.getFiles()) {
//					String fileUrl = s3FileService.uploadFile("notices", file);
//					NoticeFile noticeFile = NoticeFile.createNoticeFile(
//							file.getOriginalFilename(),
//							extractKeyFromUrl(fileUrl),
//							fileUrl,
//							file.getSize(),
//							notice
//					);
//					noticeFileRepository.save(noticeFile);
//				}
//			}
//
//			return notice.getId();
//		} catch (Exception e) {
//			log.error("Error creating notice", e);
//			throw new RuntimeException("공지사항 생성 중 오류가 발생했습니다.", e);
//		}
//	}

	// 공지사항 수정
	@Transactional
	public Long updateNotice(Long id, NoticeRequestDto requestDto) {
		try {
			Notice notice = noticeRepository.findById(id)
					.orElseThrow(() -> new EntityNotFoundException("Notice not found"));

			Course course = courseRepository.findById(requestDto.getCourseId())
					.orElseThrow(() -> new EntityNotFoundException("Course not found"));

			// 기존 파일 삭제
			if (requestDto.getDeleteFileIds() != null && !requestDto.getDeleteFileIds().isEmpty()) {
				for (Long fileId : requestDto.getDeleteFileIds()) {
					NoticeFile existingFile = noticeFileRepository.findById(fileId)
							.orElseThrow(() -> new EntityNotFoundException("File not found: " + fileId));

					// S3에서 파일 삭제
					s3FileService.deleteFile(existingFile.getFileUrl());
					noticeFileRepository.delete(existingFile);
					notice.getFiles().remove(existingFile);
				}
			}

			// 새 파일 업로드
			if (requestDto.getFiles() != null && !requestDto.getFiles().isEmpty()) {
				for (MultipartFile file : requestDto.getFiles()) {
					// S3에 파일 업로드
					String s3Key = generateS3Key(file.getOriginalFilename());
					String fileUrl = s3FileService.uploadFile("notices/files", file);

					// NoticeFile 엔티티 생성 및 저장
					NoticeFile noticeFile = NoticeFile.createNoticeFile(
							file.getOriginalFilename(),
							s3Key,
							fileUrl,
							file.getSize(),
							notice
					);
					NoticeFile savedFile = noticeFileRepository.save(noticeFile);
					notice.getFiles().add(savedFile);
				}
			}

			// 공지사항 정보 업데이트
			notice.update(
					requestDto.getTitle(),
					requestDto.getContent(),
					requestDto.getType(),
					course
			);

			return notice.getId();
		} catch (EntityNotFoundException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error updating notice: " + id, e);
			throw new RuntimeException("공지사항 수정 중 오류가 발생했습니다.", e);
		}
	}

	// S3 키 생성 메서드
	private String generateS3Key(String originalFilename) {
		return String.format("notices/files/%s-%s", UUID.randomUUID(), originalFilename);
	}
//	@Transactional
//	public Long updateNotice(Long id, NoticeRequestDto requestDto) {
//		try {
//			Notice notice = noticeRepository.findById(id)
//					.orElseThrow(() -> new EntityNotFoundException("Notice not found"));
//
//			Course course = courseRepository.findById(requestDto.getCourseId())
//					.orElseThrow(() -> new EntityNotFoundException("Course not found"));
//
//			// 기존 파일 삭제
//			List<NoticeFile> existingFiles = new ArrayList<>(notice.getFiles());
//			for (NoticeFile existingFile : existingFiles) {
//				s3FileService.deleteFile(existingFile.getFileUrl());
//				noticeFileRepository.delete(existingFile);
//				notice.getFiles().remove(existingFile);
//			}
//
//			// 새 파일 추가
//			if (requestDto.getFiles() != null && !requestDto.getFiles().isEmpty()) {
//				for (MultipartFile file : requestDto.getFiles()) {
//					String fileUrl = s3FileService.uploadFile("notices", file);
//					NoticeFile noticeFile = NoticeFile.createNoticeFile(
//							file.getOriginalFilename(),
//							extractKeyFromUrl(fileUrl),
//							fileUrl,
//							file.getSize(),
//							notice
//					);
//					NoticeFile savedFile = noticeFileRepository.save(noticeFile);
//					notice.getFiles().add(savedFile);
//				}
//			}
//
//			notice.update(
//					requestDto.getTitle(),
//					requestDto.getContent(),
//					requestDto.getType(),
//					course
//			);
//
//			return noticeRepository.save(notice).getId();
//		} catch (EntityNotFoundException e) {
//			throw e;
//		} catch (Exception e) {
//			log.error("Error updating notice: " + id, e);
//			throw new RuntimeException("공지사항 수정 중 오류가 발생했습니다.", e);
//		}
//	}

	// 공지사항 삭제
	@Transactional
	public void deleteNotice(Long noticeId) {
		try {
			Notice notice = noticeRepository.findById(noticeId)
					.orElseThrow(() -> new EntityNotFoundException("Notice not found with id: " + noticeId));

			// 1. 첨부 파일 삭제
			List<NoticeFile> attachments = noticeFileRepository.findByNoticeId(noticeId);
			deleteFiles(attachments);

			// 2. 본문 내 이미지 URL 추출 및 삭제
			Set<String> contentImageUrls = extractImageUrlsFromContent(notice.getContent());
			deleteContentImages(contentImageUrls);

			// 3. Notice 엔티티 삭제
			noticeRepository.delete(notice);

			log.info("Successfully deleted notice and all associated files. Notice ID: {}", noticeId);
		} catch (Exception e) {
			log.error("Failed to delete notice with ID: {}", noticeId, e);
			throw new RuntimeException("공지사항 삭제 중 오류가 발생했습니다.", e);
		}
	}

	private void deleteFiles(List<NoticeFile> files) {
		for (NoticeFile file : files) {
			try {
				String fileUrl = file.getFileUrl();
				log.info("Attempting to delete attachment: {}", fileUrl);
				s3FileService.deleteFile(fileUrl);
				noticeFileRepository.delete(file);
				log.info("Successfully deleted attachment: {}", fileUrl);
			} catch (Exception e) {
				log.error("Failed to delete attachment: {}", file.getFileUrl(), e);
				// 개별 파일 삭제 실패 시에도 계속 진행
			}
		}
	}

	private Set<String> extractImageUrlsFromContent(String content) {
		Set<String> imageUrls = new HashSet<>();
		if (content == null || content.isEmpty()) {
			return imageUrls;
		}

		// Quill 에디터의 이미지 URL 패턴 매칭
		String bucket = awsS3Properties.getBucket();
		String region = awsS3Properties.getRegion();
		String s3Pattern = String.format("https://%s\\.s3\\.%s\\.amazonaws\\.com/notices/images/[^\"\\s]+",
				bucket, region);

		Pattern pattern = Pattern.compile(s3Pattern);
		Matcher matcher = pattern.matcher(content);

		while (matcher.find()) {
			String imageUrl = matcher.group();
			imageUrls.add(imageUrl);
			log.info("Found image URL in content: {}", imageUrl);
		}

		return imageUrls;
	}

	private void deleteContentImages(Set<String> imageUrls) {
		for (String imageUrl : imageUrls) {
			try {
				log.info("Attempting to delete content image: {}", imageUrl);
				s3FileService.deleteFile(imageUrl);
				log.info("Successfully deleted content image: {}", imageUrl);
			} catch (Exception e) {
				log.error("Failed to delete content image: {}", imageUrl, e);
				// 개별 이미지 삭제 실패 시에도 계속 진행
			}
		}
	}

	// 단일 공지사항 조회
	@Transactional(readOnly = true)
	public NoticeResponseDto getNotice(Long noticeId) {
		try {
			Notice notice = noticeRepository.findById(noticeId)
					.orElseThrow(() -> new EntityNotFoundException("Notice not found"));

			// 파일들의 Presigned URL 생성
			if (notice.getFiles() != null && !notice.getFiles().isEmpty()) {
				for (NoticeFile file : notice.getFiles()) {
					try {
						String presignedUrl = s3FileService.generatePresignedUrl(file.getFileUrl(), Duration.ofHours(1));
						file.updateFileUrl(presignedUrl);
					} catch (Exception e) {
						log.error("Error generating presigned URL for file: " + file.getOriginalFileName(), e);
						// 파일 URL을 원본 URL로 유지
						file.updateFileUrl(file.getFileUrl());
					}
				}
			}

			List<Notice> singletonList = Collections.singletonList(notice);
			return NoticeResponseDto.fromNotices(singletonList).get(0);
		} catch (EntityNotFoundException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error fetching notice: " + noticeId, e);
			throw new RuntimeException("공지사항 조회 중 오류가 발생했습니다.", e);
		}
	}

	// 조회수 증가
	@Transactional
	public void increaseViewCount(Long noticeId) {
		try {
			Notice notice = noticeRepository.findById(noticeId)
					.orElseThrow(() -> new EntityNotFoundException("Notice not found"));

			notice.increaseViewCount();
			noticeRepository.save(notice);
		} catch (EntityNotFoundException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error increasing view count: " + noticeId, e);
			throw new RuntimeException("조회수 증가 중 오류가 발생했습니다.", e);
		}
	}

	// 전체 공지사항 조회
	public List<NoticeResponseDto> getGlobalNotices() {
		try {
			List<Notice> notices = noticeRepository.findByTypeOrderByCreatedAtDesc(NoticeType.GLOBAL);
			return NoticeResponseDto.fromNotices(notices);
		} catch (Exception e) {
			log.error("Error fetching global notices", e);
			throw new RuntimeException("전체 공지사항 조회 중 오류가 발생했습니다.", e);
		}
	}

	// 강의별 공지사항 조회
	public List<NoticeResponseDto> getCourseNotices(Long courseId) {
		try {
			List<Notice> notices = noticeRepository.findByCourseIdAndTypeOrderByCreatedAtDesc(courseId, NoticeType.COURSE);
			return NoticeResponseDto.fromNotices(notices);
		} catch (Exception e) {
			log.error("Error fetching course notices: " + courseId, e);
			throw new RuntimeException("강의 공지사항 조회 중 오류가 발생했습니다.", e);
		}
	}

	// S3 URL에서 key 추출
	private String extractKeyFromUrl(String fileUrl) {
		String prefix = String.format("https://%s.s3.%s.amazonaws.com/",
				awsS3Properties.getBucket(), awsS3Properties.getRegion());
		return fileUrl.substring(prefix.length());
	}

}
