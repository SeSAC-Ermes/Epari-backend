package com.example.epari.global.config.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.example.epari.global.exception.ErrorCode;
import com.example.epari.global.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 인증되지 않은 요청(401) 발생 시 처리를 담당하는 핸들러
 * 인증이 필요한 리소스에 인증 없이 접근 시도 시 JSON 형식의 에러 응답을 반환
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * 인증 실패 시 클라이언트에게 JSON 형식의 에러 응답을 반환
	 */
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException {

		ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.UNAUTHORIZED);

		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");

		objectMapper.writeValue(response.getWriter(), errorResponse);
	}

}
