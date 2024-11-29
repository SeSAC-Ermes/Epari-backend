package com.example.epari.course.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.epari.course.domain.Course;
import com.example.epari.course.domain.CourseContent;
import com.example.epari.course.repository.CourseContentRepository;
import com.example.epari.course.repository.CourseRepository;
import com.example.epari.global.exception.course.CourseNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class CsvImportService {

	private final CourseRepository courseRepository;
	private final CourseContentRepository courseContentRepository;
	private final S3Client s3Client;

	private static final String BUCKET_NAME = "epari-prod";
	private static final String CSV_FOLDER = "csv-data/";

	@Transactional
	public void importCsvContent(Long courseId, String csvFileName) {
		log.info("Starting CSV import from S3 for course: {}, file: {}", courseId, csvFileName);

		String s3Key = CSV_FOLDER + csvFileName;

		Course course = courseRepository.findById(courseId)
				.orElseThrow(CourseNotFoundException::new);

		try {
			GetObjectRequest getObjectRequest = GetObjectRequest.builder()
					.bucket(BUCKET_NAME)
					.key(s3Key)
					.build();

			ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);

			try (BufferedReader br = new BufferedReader(
					new InputStreamReader(s3Object, StandardCharsets.UTF_8))) {

				// Skip header line
				br.readLine();

				String line;
				int importCount = 0;
				while ((line = br.readLine()) != null) {
					try {
						String[] values = line.split(",");

						CourseContent content = CourseContent.builder()
								.title(values[1].trim())
								.content(values[2].trim())
								.date(LocalDate.parse(values[3].trim()))
								.course(course)
								.build();

						courseContentRepository.save(content);
						importCount++;
					} catch (Exception e) {
						log.error("Error processing CSV line: {}", line, e);
					}
				}

				log.info("Successfully imported {} records for course: {}", importCount, courseId);
			}
		} catch (Exception e) {
			log.error("Error importing CSV from S3 - courseId: {}, file: {}", courseId, csvFileName, e);
			throw new RuntimeException("CSV 파일 임포트 중 오류 발생", e);
		}
	}
}
