package com.example.epari.global.notification;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.example.epari.global.event.NotificationType;

import lombok.RequiredArgsConstructor;

/**
 * 템플릿 처리기 클래스
 */
@Component
@RequiredArgsConstructor
public class TemplateProcessor {

	private final SpringTemplateEngine templateEngine;

	public String processTemplate(NotificationType type, Map<String, String> properties) {
		Context context = new Context();
		properties.forEach(context::setVariable);

		return templateEngine.process(type.getTemplatePath(), context);
	}

}
