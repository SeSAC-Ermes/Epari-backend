package com.example.epari.global.common.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.epari.global.common.base.BaseUser;

public interface BaseUserRepository extends JpaRepository<BaseUser, Long> {

	/**
	 * 이메일로 사용자 찾기 (Instructor, Student 모두 포함)
	 */
	@Query("SELECT u FROM BaseUser u WHERE u.email = :email")
	Optional<BaseUser> findByEmail(@Param("email") String email);

}
