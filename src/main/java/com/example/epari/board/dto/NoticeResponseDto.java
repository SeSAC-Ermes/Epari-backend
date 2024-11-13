package com.example.epari.board.dto;

import com.example.epari.board.domain.Notice;
import com.example.epari.global.common.enums.NoticeType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;


//

@Getter
public class NoticeResponseDto {

	private Long displayNumber; // 화면에 보여줄 번호 (공지사항 타입별로 따로 관리)

	private Long id; // 실제 DB ID

	private String title;

	private String content;

	private NoticeType type;

	private Integer viewCount;

	private LocalDateTime createdAt;

	private String instructorName;

	private List<NoticeFileDto> files;

	@Builder
	public NoticeResponseDto(Long displayNumber, Long id, String title, String content,
							 NoticeType type, Integer viewCount, LocalDateTime createdAt,
							 String instructorName, List<NoticeFileDto> files) {
		this.displayNumber = displayNumber;
		this.id = id;
		this.title = title;
		this.content = content;
		this.type = type;
		this.viewCount = viewCount;
		this.createdAt = createdAt;
		this.instructorName = instructorName;
		this.files = files;
	}

	public static List<NoticeResponseDto> fromNotices(List<Notice> notices) {
		AtomicLong counter = new AtomicLong(notices.size());

		return notices.stream()
				.map(notice -> NoticeResponseDto.builder()
						.displayNumber(counter.getAndDecrement())
						.id(notice.getId())
						.title(notice.getTitle())
						.content(notice.getContent())
						.type(notice.getType())
						.viewCount(notice.getViewCount())
						.createdAt(notice.getCreatedAt())
						.instructorName(notice.getInstructor().getName())
						.files(notice.getFiles().stream()
								.map(NoticeFileDto::from)
								.collect(Collectors.toList()))
						.build())
				.collect(Collectors.toList());
	}

}
