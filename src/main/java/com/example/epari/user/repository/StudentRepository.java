package com.example.epari.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.epari.user.domain.Student;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * 학생 정보에 대한 데이터베이스 접근을 담당하는 Repository 인터페이스
 * Spring Data JPA를 통해 기본적인 CRUD 연산을 제공합니다.
 */
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByEmail(String email);

	@Query("SELECT cs.student FROM CourseStudent cs WHERE cs.course.id = :courseId")
	List<Student> findByCourseId(@Param("courseId") Long courseId);
}
