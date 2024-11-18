package com.example.epari.global.event;

import java.util.HashMap;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;

/**
 * 알림 이벤트 클래스
 */
@Getter
@Builder
public class NotificationEvent {

	private String to;

	private NotificationType type;

	private Map<String, String> properties;

	public static NotificationEvent of(String to, NotificationType type) {
		return NotificationEvent.builder()
				.to(to)
				.type(type)
				.properties(new HashMap<>())
				.build();
	}

	public NotificationEvent addProperty(String key, String value) {
		this.properties.put(key, value);
		return this;
	}

}
