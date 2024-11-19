package com.example.epari.global.config.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

@Configuration
public class AwsCognitoConfig {

	@Value("${aws.region}")
	private String region;

	@Value("${aws.s3.access-key}")
	private String accessKey;

	@Value("${aws.s3.secret-key}")
	private String secretKey;

	@Bean
	public CognitoIdentityProviderClient cognitoClient() {
		AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);

		return CognitoIdentityProviderClient.builder()
				.region(Region.of(region))
				.credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
				.build();
	}

}
