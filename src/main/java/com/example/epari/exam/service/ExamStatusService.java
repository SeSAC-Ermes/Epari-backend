package com.example.epari.exam.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.epari.exam.domain.Exam;
import com.example.epari.exam.domain.ExamResult;
import com.example.epari.exam.repository.ExamRepository;
import com.example.epari.exam.repository.ExamResultRepository;
import com.example.epari.global.common.enums.ExamStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 시험 상태 서비스
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ExamStatusService {

	private final ExamRepository examRepository;

	private final ExamResultRepository examResultRepository;

	private final GradingService gradingService;

	// 시험 상태 확인 및 업데이트
	@Scheduled(fixedDelay = 60000) // 1분마다 실행
	public void checkAndUpdateExamStatus() {
		List<Exam> expiredExams = findExpiredExams();

		for (Exam exam : expiredExams) {
			try {
				processExamEndWithNewTransaction(exam);
			} catch (Exception e) {
				log.error("시험 종료 처리 실패. examId={}", exam.getId(), e);
			}
		}
	}

	// 새로운 트랜잭션에서 시험 종료 처리
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	protected void processExamEndWithNewTransaction(Exam exam) {
		// 시험 상태를 먼저 IN_PROGRESS로 변경
		if (exam.getStatus() == ExamStatus.SCHEDULED) {
			exam.updateStatus(ExamStatus.IN_PROGRESS);
			examRepository.save(exam);
		}

		// 1. 미제출자 강제 제출 처리
		List<ExamResult> inProgressResults = examResultRepository
				.findByExamIdAndStatus(exam.getId(), ExamStatus.IN_PROGRESS);

		for (ExamResult result : inProgressResults) {
			try {
				result.submit(true);
				examResultRepository.save(result);
				log.info("시험 자동 제출 처리 완료. examId={}, studentId={}",
						exam.getId(), result.getStudent().getId());
			} catch (Exception e) {
				log.error("시험 자동 제출 실패. examId={}, studentId={}",
						exam.getId(), result.getStudent().getId(), e);
				// 개별 실패는 전체 트랜잭션을 롤백하지 않음
			}
		}

		// 2. 시험 상태를 채점중으로 변경
		exam.updateStatus(ExamStatus.GRADING);
		examRepository.save(exam);

		try {
			// 3. 채점 프로세스 시작
			startGradingProcess(exam);
		} catch (Exception e) {
			log.error("채점 실패. examId={}", exam.getId(), e);
			// 채점 실패해도 시험 종료 처리는 커밋
		}

		log.info("시험 종료 처리 완료. examId={}", exam.getId());
	}

	// 만료된 시험 조회
	private List<Exam> findExpiredExams() {
		LocalDateTime now = LocalDateTime.now();
		return examRepository.findByStatusIn(Arrays.asList(ExamStatus.SCHEDULED, ExamStatus.IN_PROGRESS))
				.stream()
				.filter(exam -> {
					LocalDateTime endTime = exam.getExamDateTime().plusMinutes(exam.getDuration());
					return now.isAfter(endTime);
				})
				.collect(Collectors.toList());
	}

	// 채점 프로세스 시작
	private void startGradingProcess(Exam exam) {
		List<ExamResult> submittedResults = examResultRepository
				.findByExamIdAndStatus(exam.getId(), ExamStatus.SUBMITTED);

		// GradingService의 채점 로직 활용
		for (ExamResult result : submittedResults) {
			try {
				gradingService.processGrading(result);
				log.info("채점 완료. examId={}, studentId={}, score={}",
						exam.getId(), result.getStudent().getId(), result.getEarnedScore());
			} catch (Exception e) {
				log.error("채점 실패. examId={}, studentId={}",
						exam.getId(), result.getStudent().getId(), e);
			}
		}

		// 채점 완료 후 시험 상태 업데이트
		exam.updateStatus(ExamStatus.GRADED);
		examRepository.save(exam);

		// 최종 종료 상태로 변경
		finalizeExam(exam);
	}

	// 시험 종료 처리
	private void finalizeExam(Exam exam) {
		exam.updateStatus(ExamStatus.COMPLETED);
		examRepository.save(exam);
		log.info("시험 종료 처리 완료. examId={}", exam.getId());
	}

}
