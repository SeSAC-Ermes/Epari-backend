package com.example.epari.global.config.aws;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * AWS S3 서비스 사용에 필요한 설정값들을 관리하는 클래스
 * application-dev.properties 파일의 'aws.s3' 프리픽스를 가진 설정들과 매핑됩니다.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "aws.s3")
public class AwsS3Properties {

	private String accessKey;

	private String secretKey;

	private String bucket;

	private String region;

}
