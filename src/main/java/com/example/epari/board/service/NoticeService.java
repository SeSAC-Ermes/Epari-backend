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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

			// S3에 파일 업로드
			if (requestDto.getFiles() != null && !requestDto.getFiles().isEmpty()) {
				for (MultipartFile file : requestDto.getFiles()) {
					String fileUrl = s3FileService.uploadFile("notices", file);
					NoticeFile noticeFile = NoticeFile.createNoticeFile(
							file.getOriginalFilename(),
							extractKeyFromUrl(fileUrl),
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

	// 공지사항 수정
	@Transactional
	public Long updateNotice(Long id, NoticeRequestDto requestDto) {
		try {
			Notice notice = noticeRepository.findById(id)
					.orElseThrow(() -> new EntityNotFoundException("Notice not found"));

			Course course = courseRepository.findById(requestDto.getCourseId())
					.orElseThrow(() -> new EntityNotFoundException("Course not found"));

			// 기존 파일 삭제
			List<NoticeFile> existingFiles = new ArrayList<>(notice.getFiles());
			for (NoticeFile existingFile : existingFiles) {
				s3FileService.deleteFile(existingFile.getFileUrl());
				noticeFileRepository.delete(existingFile);
				notice.getFiles().remove(existingFile);
			}

			// 새 파일 추가
			if (requestDto.getFiles() != null && !requestDto.getFiles().isEmpty()) {
				for (MultipartFile file : requestDto.getFiles()) {
					String fileUrl = s3FileService.uploadFile("notices", file);
					NoticeFile noticeFile = NoticeFile.createNoticeFile(
							file.getOriginalFilename(),
							extractKeyFromUrl(fileUrl),
							fileUrl,
							file.getSize(),
							notice
					);
					NoticeFile savedFile = noticeFileRepository.save(noticeFile);
					notice.getFiles().add(savedFile);
				}
			}

			notice.update(
					requestDto.getTitle(),
					requestDto.getContent(),
					requestDto.getType(),
					course
			);

			return noticeRepository.save(notice).getId();
		} catch (EntityNotFoundException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error updating notice: " + id, e);
			throw new RuntimeException("공지사항 수정 중 오류가 발생했습니다.", e);
		}
	}

	// 공지사항 삭제
	@Transactional
	public void deleteNotice(Long noticeId) {
		try {
			Notice notice = noticeRepository.findById(noticeId)
					.orElseThrow(() -> new EntityNotFoundException("Notice not found"));

			// S3에서 파일 삭제
			List<NoticeFile> files = noticeFileRepository.findByNoticeId(noticeId);
			files.forEach(file -> s3FileService.deleteFile(file.getFileUrl()));

			noticeRepository.delete(notice);
		} catch (EntityNotFoundException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error deleting notice: " + noticeId, e);
			throw new RuntimeException("공지사항 삭제 중 오류가 발생했습니다.", e);
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


//package com.example.epari.board.service;
//
//import com.example.epari.board.domain.Notice;
//import com.example.epari.board.domain.NoticeFile;
//import com.example.epari.board.dto.NoticeRequestDto;
//import com.example.epari.board.dto.NoticeResponseDto;
//import com.example.epari.board.repository.NoticeRepository;
//import com.example.epari.board.repository.NoticeFileRepository;
//import com.example.epari.course.domain.Course;
//import com.example.epari.course.repository.CourseRepository;
//import com.example.epari.global.common.enums.NoticeType;
//import com.example.epari.global.common.service.S3FileService;
//import com.example.epari.global.config.aws.AwsS3Properties;
//import com.example.epari.user.domain.Instructor;
//import com.example.epari.user.repository.InstructorRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import jakarta.persistence.EntityNotFoundException;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.time.Duration;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
//@Service
//@Transactional(readOnly = true)
//@RequiredArgsConstructor
//public class NoticeService {
//
//	private final NoticeRepository noticeRepository;
//
//	private final NoticeFileRepository noticeFileRepository;
//
//	private final CourseRepository courseRepository;
//
//	private final InstructorRepository instructorRepository;
//
//	private final S3FileService s3FileService;
//
//	private final AwsS3Properties awsS3Properties;  // S3 설정값을 가져오기 위해 추가
//
//	// 1. 공지사항 작성
//	@Transactional
//	public Long createNotice(NoticeRequestDto requestDto) {
//		Course course = courseRepository.findById(requestDto.getCourseId())
//				.orElseThrow(() -> new EntityNotFoundException("Course not found"));
//
//		Instructor instructor = instructorRepository.findById(requestDto.getInstructorId())
//				.orElseThrow(() -> new EntityNotFoundException("Instructor not found"));
//
//		Notice notice = noticeRepository.save(Notice.builder()
//				.title(requestDto.getTitle())
//				.content(requestDto.getContent())
//				.type(requestDto.getType())
//				.course(course)
//				.instructor(instructor)
//				.build());
//
//		// S3에 파일 업로드
//		if (requestDto.getFiles() != null && !requestDto.getFiles().isEmpty()) {
//			for (MultipartFile file : requestDto.getFiles()) {
//				String fileUrl = s3FileService.uploadFile("notices", file);
//				NoticeFile noticeFile = NoticeFile.createNoticeFile(
//						file.getOriginalFilename(),
//						extractKeyFromUrl(fileUrl),
//						fileUrl,
//						file.getSize(),
//						notice
//				);
//				noticeFileRepository.save(noticeFile);
//			}
//		}
//
//		return notice.getId();
//	}
//
//	// 2. 공지사항 수정
//	@Transactional
//	public Long updateNotice(Long id, NoticeRequestDto requestDto) {
//		Notice notice = noticeRepository.findById(id)
//				.orElseThrow(() -> new EntityNotFoundException("Notice not found"));
//
//		Course course = courseRepository.findById(requestDto.getCourseId())
//				.orElseThrow(() -> new EntityNotFoundException("Course not found"));
//
//		// 1. 기존 파일 전체 삭제 처리 (기존 파일을 모두 새 파일로 교체)
//		List<NoticeFile> existingFiles = new ArrayList<>(notice.getFiles());
//		for (NoticeFile existingFile : existingFiles) {
//			// S3에서 파일 삭제
//			s3FileService.deleteFile(existingFile.getFileUrl());
//			// DB에서 파일 정보 삭제
//			noticeFileRepository.delete(existingFile);
//			notice.getFiles().remove(existingFile);
//		}
//
//		// 2. 새로운 파일 추가
//		if (requestDto.getFiles() != null && !requestDto.getFiles().isEmpty()) {
//			for (MultipartFile file : requestDto.getFiles()) {
//				// S3에 새 파일 업로드
//				String fileUrl = s3FileService.uploadFile("notices", file);
//
//				NoticeFile noticeFile = NoticeFile.createNoticeFile(
//						file.getOriginalFilename(),
//						extractKeyFromUrl(fileUrl),
//						fileUrl,
//						file.getSize(),
//						notice
//				);
//				// DB에 새 파일 정보 저장
//				NoticeFile savedFile = noticeFileRepository.save(noticeFile);
//				notice.getFiles().add(savedFile);
//			}
//		}
//
//		// 3. 공지사항 정보 업데이트
//		notice.update(
//				requestDto.getTitle(),
//				requestDto.getContent(),
//				requestDto.getType(),
//				course
//		);
//
//		return noticeRepository.save(notice).getId();
//	}
//
//
//	// 3. 공지사항 삭제
//	@Transactional
//	public void deleteNotice(Long noticeId) {
//		Notice notice = noticeRepository.findById(noticeId)
//				.orElseThrow(() -> new EntityNotFoundException("Notice not found"));
//
//		// S3에서 파일 삭제
//		List<NoticeFile> files = noticeFileRepository.findByNoticeId(noticeId);
//		files.forEach(file -> s3FileService.deleteFile(file.getFileUrl()));
//
//		noticeRepository.delete(notice);
//	}
//
//	// 4. 단일 공지사항 조회 (상세보기)
//
////	@Transactional
////	public NoticeResponseDto getNotice(Long noticeId) {
////		Notice notice = noticeRepository.findById(noticeId)
////				.orElseThrow(() -> new EntityNotFoundException("Notice not found"));
////
////		// 파일들의 Presigned URL 생성
////		notice.getFiles().forEach(file -> {
////			String presignedUrl = s3FileService.generatePresignedUrl(file.getFileUrl(), Duration.ofHours(1));
////			file.updateFileUrl(presignedUrl);
////		});
////
////		List<Notice> singletonList = Collections.singletonList(notice);
////		return NoticeResponseDto.fromNotices(singletonList).get(0);
////	}
//
//
//
//	// 5. 전체 글로벌 공지사항 조회 (추가된 메서드)
//	public List<NoticeResponseDto> getGlobalNotices() {
//		List<Notice> notices = noticeRepository.findByTypeOrderByCreatedAtDesc(NoticeType.GLOBAL);
//
//		// 각 공지사항의 파일들에 대해 Presigned URL 생성
//		notices.forEach(notice ->
//				notice.getFiles().forEach(file -> {
//					String presignedUrl = s3FileService.generatePresignedUrl(file.getFileUrl(), Duration.ofHours(1));
//					file.updateFileUrl(presignedUrl);
//				})
//		);
//
//		return NoticeResponseDto.fromNotices(notices);
//	}
//
//	// 6. 코스별 공지사항 조회 (추가된 메서드)
//	public List<NoticeResponseDto> getCourseNotices(Long courseId) {
//		List<Notice> notices = noticeRepository.findByCourseIdAndTypeOrderByCreatedAtDesc(courseId, NoticeType.COURSE);
//
//		// 각 공지사항의 파일들에 대해 Presigned URL 생성
//		notices.forEach(notice ->
//				notice.getFiles().forEach(file -> {
//					String presignedUrl = s3FileService.generatePresignedUrl(file.getFileUrl(), Duration.ofHours(1));
//					file.updateFileUrl(presignedUrl);
//				})
//		);
//
//		return NoticeResponseDto.fromNotices(notices);
//	}
//
//	// S3 URL에서 key 추출하는 유틸리티 메서드
//	private String extractKeyFromUrl(String fileUrl) {
//		String prefix = String.format("https://%s.s3.%s.amazonaws.com/",
//				awsS3Properties.getBucket(), awsS3Properties.getRegion());
//		return fileUrl.substring(prefix.length());
//	}
//
//
//	// 조회수 증가 메서드
//	@Transactional
//	public void increaseViewCount(Long noticeId) {
//		Notice notice = noticeRepository.findById(noticeId)
//				.orElseThrow(() -> new EntityNotFoundException("Notice not found"));
//		notice.increaseViewCount();
//	}
//
//}
