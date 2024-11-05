package com.example.epari.lecture.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.epari.lecture.domain.Attendance;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

}
