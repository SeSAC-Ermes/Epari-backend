// NoticeService.java
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
import com.example.epari.user.domain.Instructor;
import com.example.epari.user.repository.InstructorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.multipart.MultipartFile;

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

		if (requestDto.getFiles() != null && !requestDto.getFiles().isEmpty()) {
			for (MultipartFile file : requestDto.getFiles()) {
				String storedFileName = fileService.storeFile(file);
				NoticeFile noticeFile = NoticeFile.createNoticeFile(
						file.getOriginalFilename(),
						storedFileName,
						fileService.getFileUrl(storedFileName),
						file.getSize(),
						notice
				);
				noticeFileRepository.save(noticeFile);
			}
		}

		return notice.getId();
	}

	@Transactional
	public void deleteNotice(Long noticeId) {
		Notice notice = noticeRepository.findById(noticeId)
				.orElseThrow(() -> new EntityNotFoundException("Notice not found"));

		List<NoticeFile> files = noticeFileRepository.findByNoticeId(noticeId);
		files.forEach(file -> fileService.deleteFile(file.getStoredFileName()));

		noticeRepository.delete(notice);
	}

	public List<NoticeResponseDto> getGlobalNotices() {
		List<Notice> notices = noticeRepository.findByTypeOrderByCreatedAtDesc(NoticeType.GLOBAL);
		return NoticeResponseDto.fromNotices(notices);
	}

	public List<NoticeResponseDto> getCourseNotices(Long courseId) {
		List<Notice> notices = noticeRepository.findByCourseIdOrderByCreatedAtDesc(courseId);
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
