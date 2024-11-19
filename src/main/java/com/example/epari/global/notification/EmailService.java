package com.example.epari.global.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.epari.admin.exception.NotificationException;
import com.example.epari.global.event.NotificationEvent;
import com.example.epari.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SesException;

/**
 * 이메일 전송을 담당하는 서비스 클래스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

	private final SesClient sesClient;

	private final TemplateProcessor templateProcessor;

	@Value("${aws.ses.source.email}")
	private String sender;

	/**
	 * 이벤트를 기반으로 이메일을 발송
	 */
	public void sendEmail(NotificationEvent event) {
		try {
			String content = templateProcessor.processTemplate(
					event.getType(),
					event.getProperties()
			);

			SendEmailRequest request = SendEmailRequest.builder()
					.source(sender)
					.destination(Destination.builder()
							.toAddresses(event.getTo())
							.build())
					.message(Message.builder()
							.subject(Content.builder()
									.data(event.getType().getDescription())
									.build())
							.body(Body.builder()
									.html(Content.builder()
											.data(content)
											.build())
									.build())
							.build())
					.build();

			sesClient.sendEmail(request);
			log.info("Email sent successfully to: {}", event.getTo());
		} catch (SesException e) {
			log.error("AWS SES error for: {}", event.getTo(), e);
			throw new NotificationException(ErrorCode.SES_SERVICE_ERROR);
		} catch (Exception e) {
			log.error("Notification failed for: {}", event.getTo(), e);
			throw new NotificationException(ErrorCode.NOTIFICATION_SEND_FAILED);
		}
	}

}
