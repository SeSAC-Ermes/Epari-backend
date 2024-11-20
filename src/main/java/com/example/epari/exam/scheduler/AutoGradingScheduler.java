package com.example.epari.exam.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.epari.exam.domain.ExamResult;
import com.example.epari.exam.repository.ExamResultRepository;
import com.example.epari.exam.service.GradingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 시험 자동 제출 및 채점을 처리하는 스케줄러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AutoGradingScheduler {

	private final ExamResultRepository examResultRepository;
	private final GradingService gradingService;

	/**
	 * 1분마다 실행되는 시험 자동 제출 처리
	 * 시험 시간이 종료된 시험을 자동으로 제출 처리
	 */
	@Scheduled(fixedRate = 60000)
	@Transactional
	public void autoSubmitExpiredExams() {
		LocalDateTime baseTime = LocalDateTime.now().minusMinutes(1);
		List<ExamResult> expiredExams = examResultRepository.findExpiredExams(baseTime);

		for (ExamResult result : expiredExams) {
			try {
				result.submit(true); // force submit
				log.info("Auto submitted exam - resultId: {}, studentId: {}, submittedAt: {}",
						result.getId(),
						result.getStudent().getId(),
						result.getSubmitTime());
			} catch (Exception e) {
				log.error("Failed to auto submit exam - resultId: " + result.getId(), e);
			}
		}
	}

	/**
	 * 5분마다 실행되는 자동 채점 처리
	 * 제출된 시험 중 채점되지 않은 시험을 자동으로 채점
	 */
	@Scheduled(fixedRate = 300000)
	public void autoGradeSubmittedExams() {
		LocalDateTime baseTime = LocalDateTime.now().minusMinutes(5);
		List<ExamResult> pendingExams = examResultRepository.findPendingGradingExams(baseTime);

		for (ExamResult result : pendingExams) {
			try {
				gradingService.gradeExamResult(result.getId());
				log.info("Auto graded exam - resultId: {}, totalScore: {}",
						result.getId(),
						result.getTotalScore());
			} catch (Exception e) {
				log.error("Failed to auto grade exam - resultId: " + result.getId(), e);
			}
		}
	}
}
