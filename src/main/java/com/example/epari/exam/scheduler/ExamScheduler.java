package com.example.epari.exam.scheduler;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.epari.exam.domain.Exam;
import com.example.epari.exam.domain.ExamResult;
import com.example.epari.exam.repository.ExamRepository;
import com.example.epari.exam.repository.ExamResultRepository;
import com.example.epari.exam.service.ExamStatusService;
import com.example.epari.global.common.enums.ExamStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * 시험 스케줄러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExamScheduler {

	private final ExamRepository examRepository;

	private final ExamResultRepository examResultRepository;

	private final ExamStatusService examStatusService;

	@Scheduled(fixedRate = 60000)
	@Transactional
	public void processExams() {
		if (!hasActiveExams()) {
			log.debug("No active exams found, skipping scheduler");
			return;
		}

		LocalDateTime now = LocalDateTime.now();

		List<Exam> scheduledExams = examRepository.findByStatusIn(Collections.singletonList(ExamStatus.SCHEDULED));
		for (Exam exam : scheduledExams) {
			if (!exam.isBeforeExam() && !exam.isAfterExam()) {
				exam.updateStatus(ExamStatus.IN_PROGRESS);
				examRepository.save(exam);
				log.info("Exam {} status changed to IN_PROGRESS", exam.getId());
			}
		}

		List<ExamResult> expiredResults = examResultRepository.findExpiredExams(now);

		if (!expiredResults.isEmpty()) {
			log.info("Found {} expired exams to process", expiredResults.size());
			for (ExamResult result : expiredResults) {
				try {
					// ExamStatusService의 기존 로직 활용
					examStatusService.processExamEnd(result.getExam());
					log.info("Exam processed successfully - examId: {}", result.getExam().getId());
				} catch (Exception e) {
					log.error("Failed to process exam - examId: " + result.getExam().getId(), e);
				}
			}
		}
	}

	private boolean hasActiveExams() {
		return !examRepository.findByStatusIn(
				Arrays.asList(ExamStatus.SCHEDULED, ExamStatus.IN_PROGRESS)
		).isEmpty();
	}

}
