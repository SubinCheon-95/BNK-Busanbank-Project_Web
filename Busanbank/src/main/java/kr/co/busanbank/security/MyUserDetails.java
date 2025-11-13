package kr.co.busanbank.security;

import kr.co.busanbank.dto.UsersDTO;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@Builder
public class MyUserDetails implements UserDetails {

    private UsersDTO usersDTO;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 계정 목록 리스트 생성, 인가 처리에 사용
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_"+ usersDTO.getRole())); // 계정 권한 앞에 접두에 ROLE_ 작성!!!
        return authorities;
    }

    @Override
    public String getPassword() {
        return usersDTO.getUserPw();
    }

    @Override
    public String getUsername() {
        return usersDTO.getUserId();
    }

    @Override
    public boolean isAccountNonExpired() {
        // 계정 만료 여부(true: 만료안됨, false: 만료)
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // 비밀번호 만료 여부
        return true;
    }

    @Override
    public boolean isEnabled() {
        // 계정 활성화 여부
        return true;
    }

}
