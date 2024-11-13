package com.example.epari.board.service;

import com.example.epari.board.domain.Notice;
import com.example.epari.board.domain.NoticeFile;
import com.example.epari.board.dto.NoticeRequestDto;
import com.example.epari.board.dto.NoticeResponseDto;
import com.example.epari.board.repository.NoticeRepository;
import com.example.epari.board.repository.NoticeFileRepository;
import com.example.epari.course.domain.Course;
import com.example.epari.course.repository.CourseRepository;
import com.example.epari.global.common.enums.NoticeType;
import com.example.epari.global.common.service.S3FileService;
import com.example.epari.global.config.aws.AwsS3Properties;
import com.example.epari.user.domain.Instructor;
import com.example.epari.user.repository.InstructorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NoticeService {

	private final NoticeRepository noticeRepository;
	private final NoticeFileRepository noticeFileRepository;
	private final CourseRepository courseRepository;
	private final InstructorRepository instructorRepository;
	private final S3FileService s3FileService;
	private final AwsS3Properties awsS3Properties;  // S3 설정값을 가져오기 위해 추가

	// 1. 공지사항 작성
	@Transactional
	public Long createNotice(NoticeRequestDto requestDto) {
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
	}

	// 2. 공지사항 수정
	@Transactional
	public Long updateNotice(Long id, NoticeRequestDto requestDto) {
		Notice notice = noticeRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Notice not found"));

		Course course = courseRepository.findById(requestDto.getCourseId())
				.orElseThrow(() -> new EntityNotFoundException("Course not found"));

		// 삭제될 파일 처리
		if (requestDto.getDeleteFileIds() != null && !requestDto.getDeleteFileIds().isEmpty()) {
			for (Long fileId : requestDto.getDeleteFileIds()) {
				NoticeFile fileToDelete = notice.getFiles().stream()
						.filter(file -> file.getId().equals(fileId))
						.findFirst()
						.orElseThrow(() -> new EntityNotFoundException("File not found"));

				// S3에서 파일 삭제
				s3FileService.deleteFile(fileToDelete.getFileUrl());
				notice.getFiles().remove(fileToDelete);
			}
		}

		// 새로운 파일 추가
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
				notice.getFiles().add(noticeFile);
			}
		}

		notice.update(
				requestDto.getTitle(),
				requestDto.getContent(),
				requestDto.getType(),
				course
		);

		return notice.getId();
	}

	// 3. 공지사항 삭제
	@Transactional
	public void deleteNotice(Long noticeId) {
		Notice notice = noticeRepository.findById(noticeId)
				.orElseThrow(() -> new EntityNotFoundException("Notice not found"));

		// S3에서 파일 삭제
		List<NoticeFile> files = noticeFileRepository.findByNoticeId(noticeId);
		files.forEach(file -> s3FileService.deleteFile(file.getFileUrl()));

		noticeRepository.delete(notice);
	}

	// 4. 단일 공지사항 조회 (상세보기)
	@Transactional
	public NoticeResponseDto getNotice(Long noticeId) {
		Notice notice = noticeRepository.findById(noticeId)
				.orElseThrow(() -> new EntityNotFoundException("Notice not found"));

		notice.increaseViewCount();  // 조회수 증가

		// 파일들의 Presigned URL 생성
		notice.getFiles().forEach(file -> {
			String presignedUrl = s3FileService.generatePresignedUrl(file.getFileUrl(), Duration.ofHours(1));
			file.updateFileUrl(presignedUrl);
		});

		List<Notice> singletonList = Collections.singletonList(notice);
		return NoticeResponseDto.fromNotices(singletonList).get(0);
	}

	// 5. 전체 글로벌 공지사항 조회 (추가된 메서드)
	public List<NoticeResponseDto> getGlobalNotices() {
		List<Notice> notices = noticeRepository.findByTypeOrderByCreatedAtDesc(NoticeType.GLOBAL);

		// 각 공지사항의 파일들에 대해 Presigned URL 생성
		notices.forEach(notice ->
				notice.getFiles().forEach(file -> {
					String presignedUrl = s3FileService.generatePresignedUrl(file.getFileUrl(), Duration.ofHours(1));
					file.updateFileUrl(presignedUrl);
				})
		);

		return NoticeResponseDto.fromNotices(notices);
	}

	// 6. 코스별 공지사항 조회 (추가된 메서드)
	public List<NoticeResponseDto> getCourseNotices(Long courseId) {
		List<Notice> notices = noticeRepository.findByCourseIdAndTypeOrderByCreatedAtDesc(courseId, NoticeType.COURSE);

		// 각 공지사항의 파일들에 대해 Presigned URL 생성
		notices.forEach(notice ->
				notice.getFiles().forEach(file -> {
					String presignedUrl = s3FileService.generatePresignedUrl(file.getFileUrl(), Duration.ofHours(1));
					file.updateFileUrl(presignedUrl);
				})
		);

		return NoticeResponseDto.fromNotices(notices);
	}

	// S3 URL에서 key 추출하는 유틸리티 메서드
	private String extractKeyFromUrl(String fileUrl) {
		String prefix = String.format("https://%s.s3.%s.amazonaws.com/",
				awsS3Properties.getBucket(), awsS3Properties.getRegion());
		return fileUrl.substring(prefix.length());
	}
}

// 클로드 코드1
//package com.example.epari.board.service;
//
//import com.example.epari.board.domain.Notice;
//import com.example.epari.board.domain.NoticeFile;
//import com.example.epari.board.dto.FileInfoDto;
//import com.example.epari.board.dto.NoticeRequestDto;
//import com.example.epari.board.dto.NoticeResponseDto;
//import com.example.epari.board.repository.NoticeRepository;
//import com.example.epari.board.repository.NoticeFileRepository;
//import com.example.epari.course.domain.Course;
//import com.example.epari.course.repository.CourseRepository;
//import com.example.epari.global.common.enums.NoticeType;
//import com.example.epari.global.common.service.S3FileService;
//import com.example.epari.user.domain.Instructor;
//import com.example.epari.user.repository.InstructorRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import jakarta.persistence.EntityNotFoundException;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
//@Service
//@Transactional(readOnly = true)
//@RequiredArgsConstructor
//
//public class NoticeService {
//	private final NoticeRepository noticeRepository;
//	private final NoticeFileRepository noticeFileRepository;
//	private final CourseRepository courseRepository;
//	private final InstructorRepository instructorRepository;
//	private final S3FileService s3FileService;  // FileService 대신 S3FileService 사용
//
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
//						extractKeyFromUrl(fileUrl),  // stored filename은 S3 key로 사용
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
//	@Transactional
//	public Long updateNotice(Long id, NoticeRequestDto requestDto) {
//		Notice notice = noticeRepository.findById(id)
//				.orElseThrow(() -> new EntityNotFoundException("Notice not found"));
//
//		Course course = courseRepository.findById(requestDto.getCourseId())
//				.orElseThrow(() -> new EntityNotFoundException("Course not found"));
//
//		// 삭제될 파일 처리
//		if (requestDto.getDeleteFileIds() != null && !requestDto.getDeleteFileIds().isEmpty()) {
//			for (Long fileId : requestDto.getDeleteFileIds()) {
//				NoticeFile fileToDelete = notice.getFiles().stream()
//						.filter(file -> file.getId().equals(fileId))
//						.findFirst()
//						.orElseThrow(() -> new EntityNotFoundException("File not found"));
//
//				// S3에서 파일 삭제
//				s3FileService.deleteFile(fileToDelete.getFileUrl());
//				notice.getFiles().remove(fileToDelete);
//			}
//		}
//
//		// 새로운 파일 추가
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
//				notice.getFiles().add(noticeFile);
//			}
//		}
//
//		notice.update(
//				requestDto.getTitle(),
//				requestDto.getContent(),
//				requestDto.getType(),
//				course
//		);
//
//		return notice.getId();
//	}
//
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
//	@Transactional
//	public NoticeResponseDto getNotice(Long noticeId) {
//		Notice notice = noticeRepository.findById(noticeId)
//				.orElseThrow(() -> new EntityNotFoundException("Notice not found"));
//
//		notice.increaseViewCount();
//
//		// Presigned URL 생성
//		notice.getFiles().forEach(file -> {
//			String presignedUrl = s3FileService.generatePresignedUrl(file.getFileUrl(), Duration.ofHours(1));
//			file.updateFileUrl(presignedUrl);
//		});
//
//		List<Notice> singletonList = Collections.singletonList(notice);
//		return NoticeResponseDto.fromNotices(singletonList).get(0);
//	}
//
//	// S3 URL에서 key 추출
//	private String extractKeyFromUrl(String fileUrl) {
//		String prefix = String.format("https://%s.s3.%s.amazonaws.com/",
//				awsS3Properties.getBucket(), awsS3Properties.getRegion());
//		return fileUrl.substring(prefix.length());
//	}
//}


// 원래 내코드
//	private final NoticeRepository noticeRepository;
//
//	private final NoticeFileRepository noticeFileRepository;
//
//	private final CourseRepository courseRepository;
//
//	private final InstructorRepository instructorRepository;
//
//	private final FileService fileService;
//
//	private static final String UPLOAD_DIR = "C:/Users/MZC-USER/Desktop/pj";
//
//
//
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
//		// 파일 처리
//		if (requestDto.getFiles() != null && !requestDto.getFiles().isEmpty()) {
//			for (MultipartFile file : requestDto.getFiles()) {
//				FileInfoDto fileInfo = fileService.uploadFile(file);
//				NoticeFile noticeFile = NoticeFile.createNoticeFile(
//						fileInfo.getOriginalFileName(),
//						fileInfo.getStoredFileName(),
//						fileInfo.getFileUrl(),
//						fileInfo.getFileSize(),
//						notice
//				);
//				noticeFileRepository.save(noticeFile);
//			}
//		}
//
//		return notice.getId();
//	}
//
//	@Transactional
//	public Long updateNotice(Long id, NoticeRequestDto requestDto) {
//		Notice notice = noticeRepository.findById(id)
//				.orElseThrow(() -> new EntityNotFoundException("Notice not found"));
//
//		Course course = courseRepository.findById(requestDto.getCourseId())
//				.orElseThrow(() -> new EntityNotFoundException("Course not found"));
//
//		// 삭제될 파일 처리
//		if (requestDto.getDeleteFileIds() != null && !requestDto.getDeleteFileIds().isEmpty()) {
//			for (Long fileId : requestDto.getDeleteFileIds()) {
//				NoticeFile fileToDelete = notice.getFiles().stream()
//						.filter(file -> file.getId().equals(fileId))
//						.findFirst()
//						.orElseThrow(() -> new EntityNotFoundException("File not found"));
//
//				// 실제 파일 삭제
//				fileService.deleteFile(fileToDelete.getStoredFileName());
//				notice.getFiles().remove(fileToDelete);
//			}
//		}
//
//		// 새로운 파일 추가
//		if (requestDto.getFiles() != null && !requestDto.getFiles().isEmpty()) {
//			for (MultipartFile file : requestDto.getFiles()) {
//				FileInfoDto fileInfo = fileService.uploadFile(file);
//				NoticeFile noticeFile = NoticeFile.createNoticeFile(
//						fileInfo.getOriginalFileName(),
//						fileInfo.getStoredFileName(),
//						fileInfo.getFileUrl(),
//						fileInfo.getFileSize(),
//						notice
//				);
//				notice.getFiles().add(noticeFile);
//			}
//		}
//
//		// 공지사항 정보 업데이트
//		notice.update(
//				requestDto.getTitle(),
//				requestDto.getContent(),
//				requestDto.getType(),
//				course
//		);
//
//		return notice.getId();
//	}
//
//	@Transactional
//	public void deleteNotice(Long noticeId) {
//		Notice notice = noticeRepository.findById(noticeId)
//				.orElseThrow(() -> new EntityNotFoundException("Notice not found"));
//
//		// 연관된 파일들 삭제
//		List<NoticeFile> files = noticeFileRepository.findByNoticeId(noticeId);
//		files.forEach(file -> fileService.deleteFile(file.getStoredFileName()));
//
//		noticeRepository.delete(notice);
//	}
//
//	public List<NoticeResponseDto> getGlobalNotices() {
//		List<Notice> notices = noticeRepository.findByTypeOrderByCreatedAtDesc(NoticeType.GLOBAL);
//		return NoticeResponseDto.fromNotices(notices);
//	}
//
//	public List<NoticeResponseDto> getCourseNotices(Long courseId) {
//		List<Notice> notices = noticeRepository.findByCourseIdAndTypeOrderByCreatedAtDesc(courseId, NoticeType.COURSE);
//		return NoticeResponseDto.fromNotices(notices);
//	}
//
//	@Transactional
//	public NoticeResponseDto getNotice(Long noticeId) {
//		Notice notice = noticeRepository.findById(noticeId)
//				.orElseThrow(() -> new EntityNotFoundException("Notice not found"));
//
//		notice.increaseViewCount();
//
//		List<Notice> singletonList = Collections.singletonList(notice);
//		return NoticeResponseDto.fromNotices(singletonList).get(0);
//	}
//
//}
