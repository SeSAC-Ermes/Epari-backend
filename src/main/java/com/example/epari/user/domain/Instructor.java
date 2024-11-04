package com.example.epari.user.domain;

import com.example.epari.global.common.base.BaseUser;
import com.example.epari.global.common.enums.UserRole;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("INSTRUCTOR")
@PrimaryKeyJoinColumn(name = "instructor_id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Instructor extends BaseUser {

	private String careerHistory;

	private Instructor(String email, String password, String name, String phoneNumber) {
		super(email, password, name, phoneNumber, UserRole.INSTRUCTOR);
	}

	public static Instructor createInstructor(String email, String password, String name, String phoneNumber) {
		return new Instructor(email, password, name, phoneNumber);
	}

}
