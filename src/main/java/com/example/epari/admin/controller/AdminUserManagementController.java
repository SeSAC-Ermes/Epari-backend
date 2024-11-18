package com.example.epari.admin.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.epari.admin.dto.ApprovalRequestDTO;
import com.example.epari.admin.dto.CognitoUserDTO;
import com.example.epari.admin.service.AdminUserService;
import com.example.epari.admin.service.CognitoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 관리자를 위한 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Slf4j
public class AdminUserManagementController {

	private final CognitoService cognitoService;

	private final AdminUserService adminUserService;

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
	 * 임시 그룹에 속한 사용자를 승인하는 엔드포인트
	 * 특정 강의와의 매핑 작업을 수행
	 */
	@PostMapping("/{userEmail}/approve")
	public ResponseEntity<Void> approveUser(
			@PathVariable("userEmail") String email,
			@RequestBody ApprovalRequestDTO request
	) {
		// 1. 백엔드 DB에 승인 상태 업데이트
		adminUserService.approveUser(email, request);

		// 2. Cognito 그룹 변경
		cognitoService.changeUserGroup(email, "STUDENT");

		// 3. TODO 이메일 발송

		return ResponseEntity.ok().build();
	}

	/**
	 * 임시 그룹에 속한 사용자를 반려하는 엔드포인트
	 */
	@PostMapping("/{userEmail}/reject")
	public ResponseEntity<Void> rejectUser(@PathVariable("userEmail") String email) {
		// 1. Cognito에서 사용자 삭제
		cognitoService.deleteUser(email);

		// 2. TODO 이메일 발송

		return ResponseEntity.ok().build();
	}

}
