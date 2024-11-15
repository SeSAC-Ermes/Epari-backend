// NoticeController.java
package com.example.epari.board.controller;

import com.example.epari.board.dto.NoticeRequestDto;
import com.example.epari.board.dto.NoticeResponseDto;
import com.example.epari.board.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

	private final NoticeService noticeService;

	// 공지사항 작성
	@PostMapping
	public ResponseEntity<Long> createNotice(@ModelAttribute NoticeRequestDto requestDto) {
		Long noticeId = noticeService.createNotice(requestDto);
		return ResponseEntity.ok(noticeId);
	}

	// 각각의 공지사항 1개 조회
	@GetMapping("/{id}")
	public ResponseEntity<NoticeResponseDto> getNotice(@PathVariable Long id) {
		NoticeResponseDto notice = noticeService.getNotice(id);
		return ResponseEntity.ok(notice);
	}

	// 각각의 공지사항 수정
	@PutMapping("/{id}")
	public ResponseEntity<Long> updateNotice(@PathVariable Long id, @ModelAttribute NoticeRequestDto requestDto) {
		Long updatedNoticeId = noticeService.updateNotice(id, requestDto);
		return ResponseEntity.ok(updatedNoticeId);
	}


	// 공지사항 삭제
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteNotice(@PathVariable Long id) {
		try {    // 예외처리 추가
			noticeService.deleteNotice(id);
			return ResponseEntity.ok().build();
		} catch (RuntimeException e) {
			return ResponseEntity.notFound().build();
		}
	}

	// global 공지사항 전체 조회
	@GetMapping("/global")
	public ResponseEntity<List<NoticeResponseDto>> getGlobalNotices() {
		List<NoticeResponseDto> notices = noticeService.getGlobalNotices();
		return ResponseEntity.ok(notices);
	}


	// course 공지사항 전체 조회
	@GetMapping("/course/{courseId}")
	public ResponseEntity<List<NoticeResponseDto>> getCourseNotices(@PathVariable Long courseId) {
		List<NoticeResponseDto> notices = noticeService.getCourseNotices(courseId);
		return ResponseEntity.ok(notices);
	}


	// 조회수 증가 엔드포인트
	@PutMapping("/{id}/view-count")
	public ResponseEntity<Void> increaseViewCount(@PathVariable Long id) {
		try {
			noticeService.increaseViewCount(id);
			return ResponseEntity.ok().build();
		} catch (RuntimeException e) {
			return ResponseEntity.notFound().build();
		}
	}


}
