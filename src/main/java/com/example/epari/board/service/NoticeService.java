package com.example.epari.board.service;

import com.example.epari.board.domain.Notice;
import com.example.epari.board.domain.NoticeFile;
import com.example.epari.board.dto.FileInfoDto;
import com.example.epari.board.dto.NoticeRequestDto;
import com.example.epari.board.dto.NoticeResponseDto;
import com.example.epari.board.repository.NoticeRepository;
import com.example.epari.board.repository.NoticeFileRepository;
import com.example.epari.course.domain.Course;
import com.example.epari.course.repository.CourseRepository;
import com.example.epari.global.common.enums.NoticeType;
import com.example.epari.user.domain.Instructor;
import com.example.epari.user.repository.InstructorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
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

	private final FileService fileService;

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

		// 파일 처리
		if (requestDto.getFiles() != null && !requestDto.getFiles().isEmpty()) {
			for (MultipartFile file : requestDto.getFiles()) {
				FileInfoDto fileInfo = fileService.uploadFile(file);
				NoticeFile noticeFile = NoticeFile.createNoticeFile(
						fileInfo.getOriginalFileName(),
						fileInfo.getStoredFileName(),
						fileInfo.getFileUrl(),
						fileInfo.getFileSize(),
						notice
				);
				noticeFileRepository.save(noticeFile);
			}
		}

		return notice.getId();
	}

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

				// 실제 파일 삭제
				fileService.deleteFile(fileToDelete.getStoredFileName());
				notice.getFiles().remove(fileToDelete);
			}
		}

		// 새로운 파일 추가
		if (requestDto.getFiles() != null && !requestDto.getFiles().isEmpty()) {
			for (MultipartFile file : requestDto.getFiles()) {
				FileInfoDto fileInfo = fileService.uploadFile(file);
				NoticeFile noticeFile = NoticeFile.createNoticeFile(
						fileInfo.getOriginalFileName(),
						fileInfo.getStoredFileName(),
						fileInfo.getFileUrl(),
						fileInfo.getFileSize(),
						notice
				);
				notice.getFiles().add(noticeFile);
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
	}

	@Transactional
	public void deleteNotice(Long noticeId) {
		Notice notice = noticeRepository.findById(noticeId)
				.orElseThrow(() -> new EntityNotFoundException("Notice not found"));

		// 연관된 파일들 삭제
		List<NoticeFile> files = noticeFileRepository.findByNoticeId(noticeId);
		files.forEach(file -> fileService.deleteFile(file.getStoredFileName()));

		noticeRepository.delete(notice);
	}

	public List<NoticeResponseDto> getGlobalNotices() {
		List<Notice> notices = noticeRepository.findByTypeOrderByCreatedAtDesc(NoticeType.GLOBAL);
		return NoticeResponseDto.fromNotices(notices);
	}

	public List<NoticeResponseDto> getCourseNotices(Long courseId) {
		List<Notice> notices = noticeRepository.findByCourseIdAndTypeOrderByCreatedAtDesc(courseId, NoticeType.COURSE);
		return NoticeResponseDto.fromNotices(notices);
	}

	@Transactional
	public NoticeResponseDto getNotice(Long noticeId) {
		Notice notice = noticeRepository.findById(noticeId)
				.orElseThrow(() -> new EntityNotFoundException("Notice not found"));

		notice.increaseViewCount();

		List<Notice> singletonList = Collections.singletonList(notice);
		return NoticeResponseDto.fromNotices(singletonList).get(0);
	}

}
