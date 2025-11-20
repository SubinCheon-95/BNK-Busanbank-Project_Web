package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.AdminDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 작성자: 진원
 * 작성일: 2025-11-16
 * 수정일: 2025-11-20 (로그인 실패 제한 기능 추가)
 * 설명: 관리자 계정 관리 Mapper 인터페이스
 */
@Mapper
public interface AdminMapper {

    // 관리자 목록 조회 (페이징)
    List<AdminDTO> selectAdminList(
            @Param("offset") int offset,
            @Param("limit") int limit,
            @Param("searchKeyword") String searchKeyword
    );

    // 관리자 전체 개수
    int countAdmins(@Param("searchKeyword") String searchKeyword);

    // 관리자 ID로 조회
    AdminDTO selectAdminById(@Param("adminId") int adminId);

    // 관리자 추가
    int insertAdmin(AdminDTO adminDTO);

    // 관리자 수정
    int updateAdmin(AdminDTO adminDTO);

    // 관리자 삭제 (soft delete)
    int deleteAdmin(@Param("adminId") int adminId);

    // 로그인 ID 중복 체크
    int countByLoginId(@Param("loginId") String loginId);

    // ========== 로그인 실패 제한 관련 메서드 (작성자: 진원, 2025-11-20) ==========

    // LoginId로 관리자 조회
    AdminDTO selectAdminByLoginId(@Param("loginId") String loginId);

    // 로그인 실패 카운트 증가
    int incrementLoginFailCount(@Param("loginId") String loginId);

    // 계정 잠금
    int lockAccount(@Param("loginId") String loginId);

    // 로그인 성공 시 실패 카운트 리셋
    int resetLoginFailCount(@Param("loginId") String loginId);

    // 계정 잠금 해제
    int unlockAccount(@Param("loginId") String loginId);
}
