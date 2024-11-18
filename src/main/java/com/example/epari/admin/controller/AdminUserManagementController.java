package com.example.epari.admin.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.epari.admin.dto.CognitoUserDTO;
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

	/**
	 * 임시 그룹에 속한 사용자를 조회하는 엔드포인트
	 */
	@GetMapping("/pending")
	public ResponseEntity<List<CognitoUserDTO>> getPendingUsers() {
		List<CognitoUserDTO> pendingUsers =
				cognitoService.getUsersByGroup("PENDING_ROLES");

		return ResponseEntity.ok(pendingUsers);
	}

}
