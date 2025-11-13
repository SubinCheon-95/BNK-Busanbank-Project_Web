package kr.co.busanbank.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

@Configuration
public class SecurityConfig {

    @Autowired
    private MyUserDetailsService myUserDetailsService;

    private final CustomLoginSuccessHandler successHandler = new CustomLoginSuccessHandler();

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 로그인 설정
        http.formLogin(form -> form
                .loginPage("/member/login")
                .successHandler(successHandler)
                .failureUrl("/member/login?error=true")
                .usernameParameter("userId")
                .passwordParameter("userPw")
        );

        // 로그아웃 설정
        http.logout(logout -> logout
                .logoutUrl("/member/logout")
                .invalidateHttpSession(true)
                .logoutSuccessUrl("/member/login?logout=true"));

        // 자동 로그인
        /* http.rememberMe(rem -> rem
                .key("uniqueKey")
                .tokenValiditySeconds(86400)
                .userDetailsService(myUserDetailsService)
        ); */  // 임시로 자동로그인 주석처리했습니다.

        // 세션 만료 시 이동
        http.sessionManagement(session -> session
                .invalidSessionUrl("/member/login?timeout=true")
        );

         //인가 설정
        http.authorizeHttpRequests(authorize -> authorize
//                .requestMatchers("/admin/cs/**").hasRole("2")
//                .requestMatchers("/admin/**").hasAnyRole("3","2")
                .requestMatchers("/my/**").hasAnyRole("USER")
                .anyRequest().permitAll()
        );

        // 권한 없는 사용자 접근
        http.exceptionHandling(exception -> exception
                .accessDeniedHandler(new CustomAccessDeniedHandler())
        );



        // 기타 설정
        http.csrf(CsrfConfigurer::disable);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

}