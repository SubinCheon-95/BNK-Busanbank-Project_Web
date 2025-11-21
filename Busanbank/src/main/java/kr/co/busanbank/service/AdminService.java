package kr.co.busanbank.service;

import kr.co.busanbank.dto.AdminDTO;
import kr.co.busanbank.dto.SecuritySettingDTO;
import kr.co.busanbank.mapper.AdminMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 작성자: 진원
 * 작성일: 2025-11-16
 * 수정일: 2025-11-20 (보안 설정 적용)
 * 설명: 관리자 계정 관리 서비스
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AdminService {

    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;
    private final SecuritySettingService securitySettingService;

    /**
     * 관리자 목록 조회 (페이징)
     */
    public List<AdminDTO> getAdminList(int page, int size, String searchKeyword) {
        int offset = (page - 1) * size;
        return adminMapper.selectAdminList(offset, size, searchKeyword);
    }

    /**
     * 관리자 전체 개수
     */
    public int getTotalCount(String searchKeyword) {
        return adminMapper.countAdmins(searchKeyword);
    }

    /**
     * 관리자 ID로 조회
     */
    public AdminDTO getAdminById(int adminId) {
        return adminMapper.selectAdminById(adminId);
    }

    /**
     * 관리자 추가
     * 작성자: 진원, 2025-11-20 (비밀번호 정책 검증 추가)
     */
    public boolean createAdmin(AdminDTO adminDTO) {
        try {
            // 비밀번호 정책 검증
            validatePassword(adminDTO.getPassword());

            // 비밀번호 암호화
            String encodedPassword = passwordEncoder.encode(adminDTO.getPassword());
            adminDTO.setPassword(encodedPassword);

            int result = adminMapper.insertAdmin(adminDTO);
            return result > 0;
        } catch (IllegalArgumentException e) {
            log.error("비밀번호 정책 위반: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("관리자 추가 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 관리자 수정
     * 작성자: 진원, 2025-11-20 (비밀번호 정책 검증 추가)
     */
    public boolean updateAdmin(AdminDTO adminDTO) {
        try {
            // 비밀번호가 있으면 검증 후 암호화
            if (adminDTO.getPassword() != null && !adminDTO.getPassword().isEmpty()) {
                // 비밀번호 정책 검증
                validatePassword(adminDTO.getPassword());

                String encodedPassword = passwordEncoder.encode(adminDTO.getPassword());
                adminDTO.setPassword(encodedPassword);
            }

            int result = adminMapper.updateAdmin(adminDTO);
            return result > 0;
        } catch (IllegalArgumentException e) {
            log.error("비밀번호 정책 위반: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("관리자 수정 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 관리자 삭제 (soft delete)
     */
    public boolean deleteAdmin(int adminId) {
        try {
            int result = adminMapper.deleteAdmin(adminId);
            return result > 0;
        } catch (Exception e) {
            log.error("관리자 삭제 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 로그인 ID 중복 체크
     */
    public boolean isLoginIdDuplicate(String loginId) {
        int count = adminMapper.countByLoginId(loginId);
        return count > 0;
    }

    /**
     * 비밀번호 정책 검증
     * 작성자: 진원, 2025-11-20
     */
    private void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("비밀번호를 입력해주세요.");
        }

        try {
            // DB에서 비밀번호 최소 길이 설정 조회
            SecuritySettingDTO minLengthSetting = securitySettingService.getSettingByKey("PASSWORD_MIN_LENGTH");
            if (minLengthSetting != null) {
                int minLength = Integer.parseInt(minLengthSetting.getSettingvalue());

                if (password.length() < minLength) {
                    throw new IllegalArgumentException("비밀번호는 최소 " + minLength + "자 이상이어야 합니다.");
                }
            }
        } catch (NumberFormatException e) {
            log.error("비밀번호 최소 길이 설정 값이 잘못되었습니다: {}", e.getMessage());
            // 기본값 8자 적용
            if (password.length() < 8) {
                throw new IllegalArgumentException("비밀번호는 최소 8자 이상이어야 합니다.");
            }
        }
    }
}
