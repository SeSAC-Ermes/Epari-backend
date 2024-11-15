package com.example.epari.board.controller;

import com.example.epari.board.dto.NoticeRequestDto;
import com.example.epari.board.dto.NoticeResponseDto;
import com.example.epari.board.service.NoticeService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
@Slf4j
public class NoticeController {
	private final NoticeService noticeService;

	// 공지사항 작성
	@PostMapping
	public ResponseEntity<Long> createNotice(@ModelAttribute NoticeRequestDto requestDto) {
		try {
			Long noticeId = noticeService.createNotice(requestDto);
			return ResponseEntity.ok(noticeId);
		} catch (Exception e) {
			log.error("Error creating notice", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(null);
		}
	}

	// 공지사항 조회
	@GetMapping("/{id}")
	public ResponseEntity<?> getNotice(@PathVariable Long id) {
		try {
			NoticeResponseDto notice = noticeService.getNotice(id);
			return ResponseEntity.ok(notice);
		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ErrorResponse("공지사항을 찾을 수 없습니다."));
		} catch (Exception e) {
			log.error("Error fetching notice: " + id, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorResponse("서버 오류가 발생했습니다."));
		}
	}

	// 공지사항 수정
	@PutMapping("/{id}")
	public ResponseEntity<?> updateNotice(
			@PathVariable Long id,
			@ModelAttribute NoticeRequestDto requestDto) {
		try {
			Long updatedId = noticeService.updateNotice(id, requestDto);
			return ResponseEntity.ok(updatedId);
		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ErrorResponse("공지사항을 찾을 수 없습니다."));
		} catch (Exception e) {
			log.error("Error updating notice: " + id, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorResponse("공지사항 수정 중 오류가 발생했습니다."));
		}
	}

	// 공지사항 삭제
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteNotice(@PathVariable Long id) {
		try {
			noticeService.deleteNotice(id);
			return ResponseEntity.ok().build();
		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ErrorResponse("공지사항을 찾을 수 없습니다."));
		} catch (Exception e) {
			log.error("Error deleting notice: " + id, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorResponse("공지사항 삭제 중 오류가 발생했습니다."));
		}
	}

	// 전체 공지사항 조회
	@GetMapping("/global")
	public ResponseEntity<?> getGlobalNotices() {
		try {
			List<NoticeResponseDto> notices = noticeService.getGlobalNotices();
			return ResponseEntity.ok(notices);
		} catch (Exception e) {
			log.error("Error fetching global notices", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorResponse("전체 공지사항 조회 중 오류가 발생했습니다."));
		}
	}

	// 강의별 공지사항 조회
	@GetMapping("/course/{courseId}")
	public ResponseEntity<?> getCourseNotices(@PathVariable Long courseId) {
		try {
			List<NoticeResponseDto> notices = noticeService.getCourseNotices(courseId);
			return ResponseEntity.ok(notices);
		} catch (Exception e) {
			log.error("Error fetching course notices: " + courseId, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorResponse("강의 공지사항 조회 중 오류가 발생했습니다."));
		}
	}

	// 조회수 증가
	@PutMapping("/{id}/viewCount")
	public ResponseEntity<?> increaseViewCount(@PathVariable Long id) {
		try {
			noticeService.increaseViewCount(id);
			return ResponseEntity.ok().build();
		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ErrorResponse("공지사항을 찾을 수 없습니다."));
		} catch (Exception e) {
			log.error("Error increasing view count: " + id, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorResponse("조회수 증가 중 오류가 발생했습니다."));
		}
	}
}

@Getter
@AllArgsConstructor
class ErrorResponse {
	private String message;
}



//@RestController
//@RequestMapping("/api/notices")
//@RequiredArgsConstructor
//public class NoticeController {
//
//	private final NoticeService noticeService;
//
//	// 공지사항 작성
//	@PostMapping
//	public ResponseEntity<Long> createNotice(@ModelAttribute NoticeRequestDto requestDto) {
//		Long noticeId = noticeService.createNotice(requestDto);
//		return ResponseEntity.ok(noticeId);
//	}
//
//	// 각각의 공지사항 1개 조회
//	@GetMapping("/{id}")
//	public ResponseEntity<NoticeResponseDto> getNotice(@PathVariable Long id) {
//		NoticeResponseDto notice = noticeService.getNotice(id);
//		return ResponseEntity.ok(notice);
//	}
//
//	// 각각의 공지사항 수정
//	@PutMapping("/{id}")
//	public ResponseEntity<Long> updateNotice(@PathVariable Long id, @ModelAttribute NoticeRequestDto requestDto) {
//		Long updatedNoticeId = noticeService.updateNotice(id, requestDto);
//		return ResponseEntity.ok(updatedNoticeId);
//	}
//
//
//	// 공지사항 삭제
//	@DeleteMapping("/{id}")
//	public ResponseEntity<Void> deleteNotice(@PathVariable Long id) {
//		try {    // 예외처리 추가
//			noticeService.deleteNotice(id);
//			return ResponseEntity.ok().build();
//		} catch (RuntimeException e) {
//			return ResponseEntity.notFound().build();
//		}
//	}
//
//	// global 공지사항 전체 조회
//	@GetMapping("/global")
//	public ResponseEntity<List<NoticeResponseDto>> getGlobalNotices() {
//		List<NoticeResponseDto> notices = noticeService.getGlobalNotices();
//		return ResponseEntity.ok(notices);
//	}
//
//
//	// course 공지사항 전체 조회
//	@GetMapping("/course/{courseId}")
//	public ResponseEntity<List<NoticeResponseDto>> getCourseNotices(@PathVariable Long courseId) {
//		List<NoticeResponseDto> notices = noticeService.getCourseNotices(courseId);
//		return ResponseEntity.ok(notices);
//	}
//
//
//	// 조회수 증가 엔드포인트
//	@PutMapping("/{id}/view-count")
//	public ResponseEntity<Void> increaseViewCount(@PathVariable Long id) {
//		try {
//			noticeService.increaseViewCount(id);
//			return ResponseEntity.ok().build();
//		} catch (RuntimeException e) {
//			return ResponseEntity.notFound().build();
//		}
//	}
//
//
//}
