package com.example.epari.admin.controller;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.epari.admin.dto.ApprovalRequestDTO;
import com.example.epari.admin.dto.CognitoUserDTO;
import com.example.epari.admin.dto.RejectionRequestDTO;
import com.example.epari.admin.service.AdminUserService;
import com.example.epari.admin.service.CognitoService;
import com.example.epari.global.event.NotificationEvent;
import com.example.epari.global.event.NotificationType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 관리자를 위한 REST API 컨트롤러
 * 사용자 관리
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Slf4j
public class AdminUserManagementController {

	private final CognitoService cognitoService;

	private final AdminUserService adminUserService;

	private final ApplicationEventPublisher eventPublisher;

	/**
	 * 임시 그룹에 속한 사용자를 조회하는 엔드포인트
	 */
	@GetMapping("/pending")
	public ResponseEntity<List<CognitoUserDTO>> getPendingUsers() {
		List<CognitoUserDTO> pendingUsers =
				cognitoService.getUsersByGroup("PENDING_ROLES");

		return ResponseEntity.ok(pendingUsers);
	}

	/**
	 * 임시 그룹에 속한 수강생을 승인하는 엔드포인트
	 * 특정 강의와의 매핑 작업을 수행
	 */
	@PostMapping("/{userEmail}/approve/student")
	public ResponseEntity<Void> approveStudent(
			@PathVariable("userEmail") String email,
			@RequestBody ApprovalRequestDTO request
	) {
		// 1. 백엔드 DB에 승인 상태 업데이트
		String courseName = adminUserService.approveStudent(email, request);

		// 2. Cognito 그룹 변경
		cognitoService.changeUserGroup(request.getUsername(), "STUDENT");

		// 3. 이메일 발송
		NotificationEvent event = NotificationEvent.of(email, NotificationType.STUDENT_APPROVED)
				.addProperty("name", request.getName())
				.addProperty("courseName", courseName);

		eventPublisher.publishEvent(event);

		return ResponseEntity.ok().build();
	}

	/**
	 * 임시 그룹에 속한 수강생을 승인하는 엔드포인트
	 * 특정 강의와의 매핑 작업을 수행
	 */
	@PostMapping("/{userEmail}/approve/instructor")
	public ResponseEntity<Void> approveInstructor(
			@PathVariable("userEmail") String email,
			@RequestBody ApprovalRequestDTO request
	) {
		// 1. 백엔드 DB에 승인 상태 업데이트
		adminUserService.approveInstructor(email, request);

		// 2. Cognito 그룹 변경
		cognitoService.changeUserGroup(request.getUsername(), "INSTRUCTOR");

		// 3. 이메일 발송
		NotificationEvent event = NotificationEvent.of(email, NotificationType.INSTRUCTOR_APPROVED)
				.addProperty("name", request.getName());

		eventPublisher.publishEvent(event);

		return ResponseEntity.ok().build();
	}

	/**
	 * 임시 그룹에 속한 사용자를 반려하는 엔드포인트
	 */
	@PostMapping("/{userEmail}/reject")
	public ResponseEntity<Void> rejectUser(
			@PathVariable("userEmail") String email,
			@RequestBody RejectionRequestDTO request
	) {
		// 1. Cognito에서 사용자 삭제
		cognitoService.deleteUser(email);

		// 2. 이메일 발송
		NotificationEvent event = NotificationEvent.of(email, NotificationType.USER_REJECTED)
				.addProperty("name", request.getName())
				.addProperty("reason", request.getReason());

		eventPublisher.publishEvent(event);

		return ResponseEntity.ok().build();
	}

}
