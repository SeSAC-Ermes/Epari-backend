package com.example.epari.global.config.aws;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * AWS S3 서비스 사용을 위한 설정 클래스
 * AWS SDK v2를 사용하여 S3 클라이언트 및 Presigner를 설정합니다.
 */
@Configuration
@EnableConfigurationProperties(AwsS3Properties.class)
public class AwsS3Config {

	@Bean
	public S3Client s3Client(AwsS3Properties awsS3Properties) {
		AwsBasicCredentials credentials = AwsBasicCredentials.create(
				awsS3Properties.getAccessKey(),
				awsS3Properties.getSecretKey()
		);

		return S3Client.builder()
				.region(Region.of(awsS3Properties.getRegion()))
				.credentialsProvider(StaticCredentialsProvider.create(credentials))
				.build();
	}

	@Bean
	public S3Presigner s3Presigner(AwsS3Properties awsS3Properties) {
		AwsBasicCredentials credentials = AwsBasicCredentials.create(
				awsS3Properties.getAccessKey(),
				awsS3Properties.getSecretKey()
		);

		return S3Presigner.builder()
				.region(Region.of(awsS3Properties.getRegion()))
				.credentialsProvider(StaticCredentialsProvider.create(credentials))
				.build();
	}

}
