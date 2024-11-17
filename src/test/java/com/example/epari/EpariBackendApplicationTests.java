package com.example.epari;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import com.example.epari.global.common.service.S3FileService;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@SpringBootTest
@ActiveProfiles("test")
class EpariBackendApplicationTests {

	@MockBean
	S3Client s3Client;

	@MockBean
	S3Presigner s3Presigner;

	@MockBean
	S3FileService s3FileService;

	@Test
	void contextLoads() {
	}

}
