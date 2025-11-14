package kr.co.busanbank.security;


import kr.co.busanbank.dto.AdminDTO;
import kr.co.busanbank.dto.UsersDTO;
import kr.co.busanbank.mapper.AdminLoginMapper;
import kr.co.busanbank.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminLoginMapper adminLoginMapper;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        // 사용자가 입력한 아이디로 사용자 조회, 비밀번호에 대한 검증은 이전 컴포넌트인 AuthenticationProvider에서 수행
        AdminDTO adminDTO = adminLoginMapper.findByLoginId(loginId);
        log.info("AdminUserDetailsService loadUserByUsername loginId:{}",loginId);

        log.info("adminDTO:{}",adminDTO);

        if (adminDTO == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return AdminUserDetails.builder()
                .adminDTO(adminDTO)
                .build();
    }
}