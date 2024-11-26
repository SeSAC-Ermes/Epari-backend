package com.example.epari.admin.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.epari.admin.dto.InstructorApprovalRequestDTO;
import com.example.epari.admin.dto.StudentApprovalRequestDTO;
import com.example.epari.admin.exception.ApprovalException;
import com.example.epari.admin.exception.CognitoException;
import com.example.epari.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 사용자(학생/강사) 승인 프로세스를 관리하는 서비스 클래스
 * DB 작업과 Cognito 그룹 변경 작업의 원자성을 보장
 * 실패 시 보상 트랜잭션(Compensating Transaction)을 통해 일관성을 유지
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserApprovalManager {

	private static final String STUDENT_GROUP = "STUDENT";

	private static final String INSTRUCTOR_GROUP = "INSTRUCTOR";

	private final AdminUserService adminUserService;

	private final CognitoService cognitoService;

	/**
	 * 학생 승인 요청을 처리
	 * DB에 학생 정보를 등록하고 Cognito 사용자 그룹을 'STUDENT'로 변경
	 * Cognito 작업 실패 시 DB 변경사항을 롤백
	 */
	@Transactional
	public String approveStudent(String email, StudentApprovalRequestDTO request) {
		try {
			return processStudentApproval(email, request);
		} catch (Exception e) {
			log.error("Failed to approve student: {}", email, e);
			throw new ApprovalException(ErrorCode.APPROVAL_FAILED);
		}
	}

	/**
	 * 강사 승인 요청을 처리
	 * DB에 강사 정보를 등록하고 Cognito 사용자 그룹을 'INSTRUCTOR'로 변경
	 * Cognito 작업 실패 시 DB 변경사항을 롤백
	 */
	@Transactional
	public void approveInstructor(String email, InstructorApprovalRequestDTO request) {
		try {
			processInstructorApproval(email, request);
		} catch (Exception e) {
			log.error("Failed to approve instructor: {}", email, e);
			throw new ApprovalException(ErrorCode.APPROVAL_FAILED);
		}
	}

	private String processStudentApproval(String email, StudentApprovalRequestDTO request) {
		String courseName = adminUserService.approveStudent(email, request);

		try {
			cognitoService.changeUserGroup(request.getUsername(), STUDENT_GROUP);
			return courseName;
		} catch (CognitoException e) {
			adminUserService.rollbackStudentApproval(email);
			throw e;
		}
	}

	private void processInstructorApproval(String email, InstructorApprovalRequestDTO request) {
		adminUserService.approveInstructor(email, request);

		try {
			cognitoService.changeUserGroup(request.getUsername(), INSTRUCTOR_GROUP);
		} catch (CognitoException e) {
			adminUserService.rollbackInstructorApproval(email);
			throw e;
		}
	}

}
