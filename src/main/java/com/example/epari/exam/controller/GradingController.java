package com.example.epari.exam.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.epari.exam.exception.GradingException;
import com.example.epari.exam.exception.GradingNotPossibleException;
import com.example.epari.exam.exception.InvalidScoreException;
import com.example.epari.exam.service.GradingService;
import com.example.epari.exam.service.GradingService.ScoreStatistics;
import com.example.epari.global.annotation.CurrentUserEmail;
import com.example.epari.global.exception.ErrorCode;
import com.example.epari.global.exception.ErrorResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/**
 * 시험 채점 및 성적 통계 관련 요청을 처리하는 Controller
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/courses/{courseId}/exams/{examId}/grades")
public class GradingController {

	private final GradingService gradingService;

	/**
	 * 시험 결과 채점 요청
	 * @param courseId 강의 ID
	 * @param examId 시험 ID
	 * @param resultId 채점할 시험 결과 ID
	 * @param instructorEmail 강사 이메일
	 * @return 채점 완료 응답
	 */
	@PostMapping("/results/{resultId}")
	@PreAuthorize("hasRole('INSTRUCTOR') and @courseSecurityChecker.checkInstructorAccess(#courseId, #instructorEmail)")
	public ResponseEntity<Void> gradeExam(
			@PathVariable Long courseId,
			@PathVariable Long examId,
			@PathVariable Long resultId,
			@CurrentUserEmail String instructorEmail) {

		log.info("Grading request - courseId: {}, examId: {}, resultId: {}", courseId, examId, resultId);
		gradingService.gradeExamResult(resultId);
		return ResponseEntity.ok().build();
	}

	/**
	 * 평균 점수 조회
	 * @param courseId 강의 ID
	 * @param examId 시험 ID
	 * @param instructorEmail 강사 이메일
	 * @return 평균 점수
	 */
	@GetMapping("/average")
	@PreAuthorize("hasRole('INSTRUCTOR') and @courseSecurityChecker.checkInstructorAccess(#courseId, #instructorEmail)")
	public ResponseEntity<Double> getAverageScore(
			@PathVariable Long courseId,
			@PathVariable Long examId,
			@CurrentUserEmail String instructorEmail) {

		log.info("Retrieving average score - courseId: {}, examId: {}", courseId, examId);
		double averageScore = gradingService.calculateAverageScore(examId);
		return ResponseEntity.ok(averageScore);
	}

	/**
	 * 최고/최저 점수 통계 조회
	 * @param courseId 강의 ID
	 * @param examId 시험 ID
	 * @param instructorEmail 강사 이메일
	 * @return 점수 통계 정보
	 */
	@GetMapping("/statistics")
	@PreAuthorize("hasRole('INSTRUCTOR') and @courseSecurityChecker.checkInstructorAccess(#courseId, #instructorEmail)")
	public ResponseEntity<ScoreStatistics> getScoreStatistics(
			@PathVariable Long courseId,
			@PathVariable Long examId,
			@CurrentUserEmail String instructorEmail) {

		log.info("Retrieving score statistics - courseId: {}, examId: {}", courseId, examId);
		ScoreStatistics statistics = gradingService.calculateScoreStatistics(examId);
		return ResponseEntity.ok(statistics);
	}

	@ExceptionHandler(GradingException.class)
    public ResponseEntity<ErrorResponse> handleGradingException(GradingException e) {
        log.error("채점 처리 중 오류 발생", e);
        return ErrorResponse.toResponseEntity(ErrorCode.GRADING_FAILED);
    }

    @ExceptionHandler(GradingNotPossibleException.class)
    public ResponseEntity<ErrorResponse> handleGradingNotPossibleException(GradingNotPossibleException e) {
        log.error("채점 불가능한 상태", e);
        return ErrorResponse.toResponseEntity(ErrorCode.EXAM_NOT_SUBMITTED);
    }

    @ExceptionHandler(InvalidScoreException.class)
    public ResponseEntity<ErrorResponse> handleInvalidScoreException(InvalidScoreException e) {
        log.error("유효하지 않은 점수", e);
        return ErrorResponse.toResponseEntity(ErrorCode.INVALID_SCORE_VALUE);
    }
}
