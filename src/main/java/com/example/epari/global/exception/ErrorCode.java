package com.example.epari.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

/**
 * 애플리케이션에서 사용하는 모든 에러 코드를 정의하는 열거형
 * 각 에러의 HTTP 상태 코드, 비즈니스 코드 및 메시지를 포함
 */
@Getter
public enum ErrorCode {

	// 공통 에러 코드 (Common: CMM)
	BAD_REQUEST(HttpStatus.BAD_REQUEST, "CMM-001", "잘못된 요청입니다."),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CMM-002", "서버 내부에서 오류가 발생했습니다."),
	NOT_FOUND(HttpStatus.NOT_FOUND, "CMM-003", "요청한 리소스를 찾을 수 없습니다."),
	INVALID_INPUT(HttpStatus.BAD_REQUEST, "CMM-004", "입력값이 올바르지 않습니다."),

	// 인증, 인가 관련 에러 코드 (AUTH)
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH-001", "인증이 필요한 요청입니다."),
	FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH-002", "해당 리소스에 대한 접근 권한이 없습니다."),

	// Student 관련 에러 코드(ST
    STUDENT_NOT_FOUND(HttpStatus.NOT_FOUND, "STD-001", "학생 정보를 찾을 수 없습니다."),

	// Course 관련 에러 코드 (CRS)
	COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "CRS-001", "강의를 찾을 수 없습니다."),
	UNAUTHORIZED_COURSE_ACCESS(HttpStatus.FORBIDDEN, "CRS-002", "해당 강의에 대한 접근 권한이 없습니다."),

	// Assignment 관련 에러 코드 (ASM)
	ASSIGNMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "ASM-001", "과제를 찾을 수 없습니다."),
	SUBMISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "ASM-002", "과제 제출물을 찾을 수 없습니다."),

    // Question 관련 에러 코드(EXAM)
    QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "QST-001", "문제를 찾을 수 없습니다."),
    QUESTION_HAS_SUBMISSIONS(HttpStatus.BAD_REQUEST, "QST-002", "답안이 제출된 문제는 삭제할 수 없습니다."),

	// Exam 관련 에러 코드 (EXAM)
	EXAM_NOT_FOUND(HttpStatus.NOT_FOUND, "EXAM-001", "시험을 찾을 수 없습니다."),
	EXAM_QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "EXAM-002", "시험 문제를 찾을 수 없습니다."),
	UNAUTHORIZED_EXAM_ACCESS(HttpStatus.FORBIDDEN, "EXAM-003", "시험에 대한 접근 권한이 없습니다."),
    EXAM_QUESTION_TYPE_CHANGE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "EXAM-004", "문제 유형은 변경할 수 없습니다."),
    EXAM_NOT_STARTED(HttpStatus.BAD_REQUEST, "EXAM-005", "아직 시험 시작 시간이 아닙니다."),
    EXAM_ALREADY_ENDED(HttpStatus.BAD_REQUEST, "EXAM-006", "이미 종료된 시험입니다."),
    EXAM_TIME_EXPIRED(HttpStatus.BAD_REQUEST, "EXAM-007", "시험 시간이 종료되었습니다."),
    EXAM_ALREADY_STARTED(HttpStatus.BAD_REQUEST, "EXAM-008", "이미 시작된 시험입니다."),
    EXAM_NOT_ALL_QUESTIONS_ANSWERED(HttpStatus.BAD_REQUEST, "EXAM-009", "모든 문제에 답하지 않았습니다."),
	EXAM_ALREADY_SUBMITTED(HttpStatus.BAD_REQUEST, "EXAM-010", "이미 제출된 시험입니다."),

	// ExamResult 관련 에러 코드(RST)
	EXAM_RESULT_NOT_FOUND(HttpStatus.NOT_FOUND, "RST-001", "시험 결과를 찾을 수 없습니다."),
	EXAM_NOT_IN_PROGRESS(HttpStatus.BAD_REQUEST, "RST-002", "진행 중인 시험을 찾을 수 없습니다."),
	EXAM_RESULT_ALREADY_SUBMITTED(HttpStatus.BAD_REQUEST, "RST-003", "이미 제출된 시험입니다."),
	EXAM_RESULT_INVALID_STATUS(HttpStatus.BAD_REQUEST, "RST-004", "잘못된 시험 상태입니다."),
	EXAM_RESULT_MISSING_REQUIRED(HttpStatus.BAD_REQUEST, "RST-005", "필수 정보가 누락되었습니다.");


	private final HttpStatus status;

	private final String code;

	private final String message;

	ErrorCode(HttpStatus status, String code, String message) {
		this.status = status;
		this.code = code;
		this.message = message;
	}

}
