package com.example.epari.global.config.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

@Configuration
public class AwsCognitoConfig {

	@Value("${aws.region}")
	private String region;

	@Bean
	public CognitoIdentityProviderClient cognitoClient() {
		return CognitoIdentityProviderClient.builder()
				.region(Region.of(region))
				.build();
	}

}
