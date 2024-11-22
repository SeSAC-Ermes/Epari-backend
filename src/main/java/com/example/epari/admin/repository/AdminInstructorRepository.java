package com.example.epari.admin.repository;

import com.example.epari.admin.dto.InstructorSearchResponseDTO;
import com.example.epari.user.domain.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 관리자 - 강사 정보에 대한 데이터베이스 접근을 담당하는 레포지토리 인터페이스
 */
public interface AdminInstructorRepository extends JpaRepository<Instructor, Long> {

    /**
     * 이메일로 강사를 검색하는 쿼리 메서드
     * - 이메일이 비어있으면 전체 조회
     * - LIKE 검색으로 부분 일치도 허용
     * - 필요한 필드만 DTO로 직접 매핑
     */
    @Query("""
            SELECT new com.example.epari.admin.dto.InstructorSearchResponseDTO(
                i.id,
                u.name,
                u.email
            )
            FROM Instructor i
            JOIN BaseUser u ON u.id = i.id
            WHERE (:email IS NULL OR
                   :email = '' OR
                   LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')))
            ORDER BY u.name ASC
            """)
    List<InstructorSearchResponseDTO> searchInstructorsWithDTO(@Param("email") String email);
}
