package com.example.epari.lecture.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.epari.lecture.domain.Lecture;

//
public interface LectureRepository extends JpaRepository<Lecture, Long> {
	@Query("SELECT DISTINCT l FROM Lecture l " +
		   "LEFT JOIN FETCH l.instructor " +
		   "WHERE l.id = :lectureId")
	Optional<Lecture> findByIdWithIdInstructor(@Param("lectureId") Long lectureId);
}
