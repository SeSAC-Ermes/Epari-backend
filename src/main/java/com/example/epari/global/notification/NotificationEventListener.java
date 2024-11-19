package com.example.epari.global.notification;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.example.epari.global.event.NotificationEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 알림 이벤트를 Listen하는 이벤트 리스너 클래스
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

	private final EmailService emailService;

	/**
	 * NotificationEvent를 처리하는 메서드
	 * emailService.sendEmail 메서드 호출
	 */
	@Async
	@EventListener
	public void handleNotificationEvent(NotificationEvent event) {
		log.info("Received notification event for: {}", event.getTo());
		emailService.sendEmail(event);
	}

}
