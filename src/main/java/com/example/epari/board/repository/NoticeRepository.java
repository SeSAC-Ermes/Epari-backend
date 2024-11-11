package com.example.epari.board.repository;

import com.example.epari.board.domain.Notice;
import com.example.epari.global.common.enums.NoticeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

	List<Notice> findByTypeOrderByCreatedAtDesc(NoticeType type);

	List<Notice> findByCourseIdOrderByCreatedAtDesc(Long courseId);

	Optional<Notice> findByIdAndType(Long id, NoticeType type);

}
