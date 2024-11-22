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
	// 기본 인증 관련
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH-001", "인증이 필요한 요청입니다."),
	FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH-002", "해당 리소스에 대한 접근 권한이 없습니다."),

	// 사용자 상태 관련
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH-003", "사용자를 찾을 수 없습니다."),
	USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH-004", "이미 가입된 이메일입니다."),
	USER_NOT_CONFIRMED(HttpStatus.BAD_REQUEST, "AUTH-005", "이메일 인증이 완료되지 않은 사용자입니다."),
	USER_STATUS_INVALID(HttpStatus.BAD_REQUEST, "AUTH-006", "비밀번호를 재설정할 수 없는 사용자 상태입니다."),
	INVALID_USER_STATUS(HttpStatus.BAD_REQUEST, "AUTH-007", "유효하지 않은 사용자 상태입니다."),

	// 인증 코드 관련
	INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "AUTH-008", "잘못된 인증 코드입니다. 다시 시도해주세요."),
	VERIFICATION_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "AUTH-009", "만료된 인증 코드입니다. 코드를 재발송 해주세요."),
	VERIFICATION_CODE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "AUTH-010", "잠시 후 다시 시도해주세요."),

	// 비밀번호 관련
	INVALID_PASSWORD_RESET_REQUEST(HttpStatus.BAD_REQUEST, "AUTH-011", "유효하지 않은 비밀번호 재설정 요청입니다."),
	PASSWORD_RESET_CODE_MISMATCH(HttpStatus.BAD_REQUEST, "AUTH-012", "잘못된 비밀번호 재설정 코드입니다."),
	PASSWORD_RESET_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "AUTH-013", "만료된 비밀번호 재설정 코드입니다. 코드를 재발송 해주세요."),
	INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "AUTH-014", "비밀번호 형식이 올바르지 않습니다."),
	INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "AUTH-015", "유효하지 않은 비밀번호입니다."),
	RESET_REQUIRED_PASSWORD(HttpStatus.BAD_REQUEST, "AUTH-016", "비밀번호 재설정이 필요한 상태입니다."),

	// 요청 제한/오류 관련
	TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "AUTH-017", "너무 많은 요청이 발생했습니다. 잠시 후 다시 시도해주세요."),
	INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "AUTH-018", "잘못된 파라미터가 전달되었습니다."),
	SIGNUP_FAILED(HttpStatus.BAD_REQUEST, "AUTH-019", "회원가입 처리 중 오류가 발생했습니다."),
	PENDING_APPROVAL(HttpStatus.BAD_REQUEST, "AUTH-020", "가입 승인 대기중입니다."),

	// Student 관련 에러 코드(STD)
	STUDENT_NOT_FOUND(HttpStatus.NOT_FOUND, "STD-001", "학생 정보를 찾을 수 없습니다."),

	// Instructor 관련 에러 코드(IST)
	INSTRUCTOR_NOT_FOUND(HttpStatus.NOT_FOUND, "IST-001", "강사 정보를 찾을 수 없습니다."),

	// Course 관련 에러 코드 (CRS)
	COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "CRS-001", "강의를 찾을 수 없습니다."),
	UNAUTHORIZED_COURSE_ACCESS(HttpStatus.FORBIDDEN, "CRS-002", "해당 강의에 대한 접근 권한이 없습니다."),
	COURSE_INSTRUCTOR_MISMATCH(HttpStatus.FORBIDDEN, "CRS-003", "해당 강의에 대한 권한이 없습니다."),
	COURSE_CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "CRS-004", "강의 자료를 찾을 수 없습니다."),
	COURSE_DATE_INVALID(HttpStatus.BAD_REQUEST, "CRS-005", "유효하지 않은 강의 일정입니다."),
	COURSE_ALREADY_EXISTS(HttpStatus.CONFLICT, "CRS-006", "이미 존재하는 강의입니다."),
	COURSE_STUDENT_NOT_FOUND(HttpStatus.NOT_FOUND, "CRS-007", "해당 강의의 수강생을 찾을 수 없습니다."),

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
	EXAM_RESULT_MISSING_REQUIRED(HttpStatus.BAD_REQUEST, "RST-005", "필수 정보가 누락되었습니다."),

	// Attendance 관련 에러 코드
	ATTENDANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "ATT-001", "출석 데이터를 찾을 수 없습니다."),
	ATTENDANCE_FUTURE_COURSE(HttpStatus.BAD_REQUEST, "ATT-002", "아직 시작하지 않은 강의입니다."),

	// Cognito 관련 에러 코드 (CGT)
	COGNITO_USER_FETCH_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CGT-001", "사용자 정보 조회에 실패했습니다."),
	COGNITO_USER_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "CGT-002", "사용자 정보를 찾을 수 없습니다."),
	COGNITO_USER_DELETE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CGT-003", "사용자를 삭제하는 도중 오류가 발생했습니다."),

	// 알림 관련 에러 코드 (NTF)
	NOTIFICATION_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "NTF-001", "알림 발송에 실패했습니다."),
	SES_SERVICE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "NTF-002", "이메일 서비스 연동 중 오류가 발생했습니다."),

	// 채점 관련 에러 코드 (GRD)
	EXAM_NOT_SUBMITTED(HttpStatus.BAD_REQUEST, "GRD-001", "제출되지 않은 시험은 채점할 수 없습니다."),
	EXAM_ALREADY_GRADED(HttpStatus.BAD_REQUEST, "GRD-002", "이미 채점이 완료된 시험입니다."),
	GRADING_IN_PROGRESS(HttpStatus.BAD_REQUEST, "GRD-003", "채점이 진행 중인 시험입니다."),
	INVALID_SCORE_VALUE(HttpStatus.BAD_REQUEST, "GRD-004", "유효하지 않은 점수입니다."),
	GRADING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "GRD-005", "채점 처리 중 오류가 발생했습니다."),

	//파일 관련 에러 코드 (FILE)
	FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "FILE-001", "파일을 찾을 수 없습니다."),
	FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE-002", "파일 업로드에 실패했습니다."),
	FILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE-003", "파일 삭제에 실패했습니다."),
	FILE_DOWNLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE-004", "파일 다운로드에 실패했습니다."),
	FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "FILE-005", "파일 크기가 제한을 초과했습니다."),
	INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "FILE-006", "지원하지 않는 파일 형식입니다."),
	FILE_EMPTY(HttpStatus.BAD_REQUEST, "FILE-007", "파일이 비어있습니다."),
	INVALID_FILE_URL(HttpStatus.BAD_REQUEST, "FILE-008", "잘못된 파일 URL 형식입니다.");

	private final HttpStatus status;

	private final String code;

	private final String message;

	ErrorCode(HttpStatus status, String code, String message) {
		this.status = status;
		this.code = code;
		this.message = message;
	}

}
