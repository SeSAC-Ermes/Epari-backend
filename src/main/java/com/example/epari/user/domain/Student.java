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
@DiscriminatorValue("STUDENT")    //상속 관계에서 학생 타입을 구분하는 값
@PrimaryKeyJoinColumn(name = "student_id") //부모 테이블의 PK를 참조하는 FK 컬럼명 지정
@NoArgsConstructor(access = AccessLevel.PROTECTED) //protected 기본 생성자로 무분별한 객체 생성 방지
@Getter
public class Student extends BaseUser {

	private Student(String email, String password, String name, String phoneNumber) {
		super(email, password, name, phoneNumber, UserRole.STUDENT);
	}

	public static Student createStudent(String email, String password, String name, String phoneNumber) {
		return new Student(email, password, name, phoneNumber);
	}

}
