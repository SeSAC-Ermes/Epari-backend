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

	// Course 관련 에러 코드 (CRS)
	COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "CRS-001", "강의를 찾을 수 없습니다."),
	UNAUTHORIZED_COURSE_ACCESS(HttpStatus.FORBIDDEN, "CRS-002", "해당 강의에 대한 접근 권한이 없습니다."),

	// Assignment 관련 에러 코드 (ASM)
	ASSIGNMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "ASM-001", "과제를 찾을 수 없습니다."),
	SUBMISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "ASM-002", "과제 제출물을 찾을 수 없습니다."),

	// Exam 관련 에러 코드 (EXAM)
	EXAM_NOT_FOUND(HttpStatus.NOT_FOUND, "EXAM-001", "시험을 찾을 수 없습니다."),
	EXAM_RESULT_NOT_FOUND(HttpStatus.NOT_FOUND, "EXAM-002", "시험 결과를 찾을 수 없습니다."),

	// Attendance 관련 에러 코드
	ATTENDANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "ATT-001", "출석 데이터를 찾을 수 없습니다."),
	ATTENDANCE_FUTURE_COURSE(HttpStatus.BAD_REQUEST, "ATT-002", "아직 시작하지 않은 강의입니다.");

	private final HttpStatus status;

	private final String code;

	private final String message;

	ErrorCode(HttpStatus status, String code, String message) {
		this.status = status;
		this.code = code;
		this.message = message;
	}

}
