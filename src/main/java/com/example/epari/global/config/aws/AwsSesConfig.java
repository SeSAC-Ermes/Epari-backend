package com.example.epari.global.config.aws;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

/**
 * AWS SES 사용을 위한 설정 클래스
 */
@Configuration
@EnableConfigurationProperties(AwsS3Properties.class)
public class AwsSesConfig {

	@Bean
	public SesClient sesClient(AwsS3Properties properties) {
		return SesClient.builder()
				.region(Region.of(properties.getRegion()))
				.credentialsProvider(StaticCredentialsProvider.create(
						AwsBasicCredentials.create(
								properties.getAccessKey(),
								properties.getSecretKey()
						)
				))
				.build();
	}

}
