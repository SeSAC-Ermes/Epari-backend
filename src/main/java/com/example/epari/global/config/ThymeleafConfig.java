package com.example.epari.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

/**
 * Thymeleaf 관련 설정 클래스
 */
@Configuration
public class ThymeleafConfig {

	@Bean
	public SpringTemplateEngine emailTemplateEngine() {
		SpringTemplateEngine templateEngine = new SpringTemplateEngine();

		templateEngine.setTemplateResolver(emailTemplateResolver());

		return templateEngine;
	}

	@Bean
	public ITemplateResolver emailTemplateResolver() {
		ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();

		templateResolver.setPrefix("mail-templates/");
		templateResolver.setSuffix(".html");
		templateResolver.setTemplateMode("HTML");
		templateResolver.setCharacterEncoding("UTF-8");

		return templateResolver;
	}

}
