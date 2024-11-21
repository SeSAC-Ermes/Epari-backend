package com.example.epari.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.epari.global.annotation.CurrentUserEmail;
import com.example.epari.user.service.UserProfileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/mypage/profile")
public class UserProfileController {

	private final UserProfileService userProfileService;

	@PostMapping("/image")
	public ResponseEntity<String> uploadProfileImage(
			@CurrentUserEmail String username,
			@RequestParam("file") MultipartFile file,
			@AuthenticationPrincipal Jwt jwt) {

		String accessToken = jwt.getTokenValue();
		String imageUrl = userProfileService.updateProfileImage(username, file, accessToken);
		return ResponseEntity.ok(imageUrl);
	}

	@DeleteMapping("/image")
	public ResponseEntity<Void> deleteProfileImage(
			@CurrentUserEmail String username,
			@AuthenticationPrincipal Jwt jwt) {

		String accessToken = jwt.getTokenValue();
		userProfileService.deleteProfileImage(username, accessToken);
		return ResponseEntity.ok().build();
	}

}
