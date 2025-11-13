package kr.co.busanbank.security;


import kr.co.busanbank.dto.UsersDTO;
import kr.co.busanbank.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class MyUserDetailsService implements UserDetailsService {

    private final MemberMapper memberMapper;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        // 사용자가 입력한 아이디로 사용자 조회, 비밀번호에 대한 검증은 이전 컴포넌트인 AuthenticationProvider에서 수행
        UsersDTO usersDTO = memberMapper.findByUserId(userId);

        if (usersDTO == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return MyUserDetails.builder()
                .usersDTO(usersDTO)
                .build();
    }
}
