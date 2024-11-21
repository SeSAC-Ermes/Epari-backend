package com.example.epari.global.common.base;

import com.example.epari.global.common.enums.UserRole;
import com.example.epari.user.domain.ProfileImage;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "user_type")
@Getter
public abstract class BaseUser extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private UserRole role;

	@Embedded
	private ProfileImage profileImage;

	protected BaseUser() {
		super();
	}

	protected BaseUser(String email, String name, UserRole role) {
		this.email = email;
		this.name = name;
		this.role = role;
	}

	public void updateProfile(String name) {
		this.name = name;

	}

	public void updateProfileImage(ProfileImage profileImage) {
		this.profileImage = profileImage;
	}
}
