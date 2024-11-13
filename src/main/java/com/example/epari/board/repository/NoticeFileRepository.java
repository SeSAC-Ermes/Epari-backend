package com.example.epari.board.repository;

import com.example.epari.board.domain.NoticeFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeFileRepository extends JpaRepository<NoticeFile, Long> {

	List<NoticeFile> findByNoticeId(Long noticeId);

	void deleteByNoticeId(Long noticeId);

}
