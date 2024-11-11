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

	@PostMapping
	public ResponseEntity<Long> createNotice(@ModelAttribute NoticeRequestDto requestDto) {
		Long noticeId = noticeService.createNotice(requestDto);
		return ResponseEntity.ok(noticeId);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteNotice(@PathVariable Long id) {
		noticeService.deleteNotice(id);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/global")
	public ResponseEntity<List<NoticeResponseDto>> getGlobalNotices() {
		List<NoticeResponseDto> notices = noticeService.getGlobalNotices();
		return ResponseEntity.ok(notices);
	}

	@GetMapping("/course/{courseId}")
	public ResponseEntity<List<NoticeResponseDto>> getCourseNotices(@PathVariable Long courseId) {
		List<NoticeResponseDto> notices = noticeService.getCourseNotices(courseId);
		return ResponseEntity.ok(notices);
	}

	@GetMapping("/{id}")
	public ResponseEntity<NoticeResponseDto> getNotice(@PathVariable Long id) {
		NoticeResponseDto notice = noticeService.getNotice(id);
		return ResponseEntity.ok(notice);
	}
}
