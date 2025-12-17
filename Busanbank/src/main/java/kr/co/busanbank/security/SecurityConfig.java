package kr.co.busanbank.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import kr.co.busanbank.jwt.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private MyUserDetailsService myUserDetailsService;

    @Autowired
    private AdminUserDetailsService adminUserDetailsService;

    @Autowired
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @Autowired
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Autowired
    private CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    @Autowired
    private UserAuthenticationFailureHandler userAuthenticationFailureHandler;

    @Autowired
    private CustomLogoutHandler customLogoutHandler;

    @Autowired
    private CustomLoginSuccessHandler successHandler;

    @Autowired
    private AdminLoginSuccessHandler adminSuccessHandler;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // 자동 로그인
        /* http.rememberMe(rem -> rem
                .key("uniqueKey")
                .tokenValiditySeconds(86400)
                .userDetailsService(myUserDetailsService)
        ); */  // 임시로 자동로그인 주석처리했습니다.


    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        DaoAuthenticationProvider memberProvider = new DaoAuthenticationProvider();
        memberProvider.setUserDetailsService(myUserDetailsService);
        memberProvider.setPasswordEncoder(passwordEncoder());

        DaoAuthenticationProvider adminProvider = new DaoAuthenticationProvider();
        adminProvider.setUserDetailsService(adminUserDetailsService);
        adminProvider.setPasswordEncoder(passwordEncoder());

        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .authenticationProvider(memberProvider)
                .authenticationProvider(adminProvider)
                .build();
    }

    // 2025-12-15 / 플러터 연동 / 작성자 - 오서정
    @Bean
    @Order(0)
    public SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {

        http
                .securityMatcher("/api/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // 추후에 각자 필요한 페이지에 대해 공개접근과 인증접근 목록에 추가하시면 됩니다. 25/12/15 수진
                        // ✅ 로그인 없이 접근 가능 (공개 API) 25/12/15 수진
                        .requestMatchers("/api/member/**").permitAll()  // 회원가입, 로그인
                        .requestMatchers("/api/products").permitAll()  // 상품 목록
                        .requestMatchers("/api/products/**").permitAll()  // 상품 상세
                        .requestMatchers("/api/flutter/products/*/terms").permitAll()  // 약관 조회
                        .requestMatchers("/api/cs/faq/**").permitAll() //faq

                        // ✅ 로그인 필요한 API (JWT 인증) 25/12/15 수진
                        .requestMatchers("/api/flutter/coupons/**").hasRole("USER")  // 쿠폰 조회
                        .requestMatchers("/api/flutter/points/**").hasRole("USER")  // 포인트 조회
                        .requestMatchers("/api/flutter/join/**").hasRole("USER")  // 상품 가입
                        .requestMatchers("/api/flutter/branches").hasRole("USER")  // 지점
                        .requestMatchers("/api/flutter/branches/**").hasRole("USER")  // 지점목록
                        .requestMatchers("/api/flutter/employees").hasRole("USER")  // 직원

                        // 채팅 상담 api 25/12/17 우지희
                        .requestMatchers("/api/chat/**").hasRole("USER")

                        // 비트코인/금/오일 api 25/12/16 윤종인
                        .requestMatchers("/api/coin/history/**").permitAll()
                        .anyRequest().hasRole("USER")  // 나머지 전부 인증 필요

                )
                // ✅ JWT 필터 추가 (인증이 필요한 요청에만 적용)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain adminSecurity(HttpSecurity http) throws Exception {
        DaoAuthenticationProvider adminProvider = new DaoAuthenticationProvider();
        adminProvider.setUserDetailsService(adminUserDetailsService);
        adminProvider.setPasswordEncoder(passwordEncoder());

        AuthenticationManager adminAuthManager = new ProviderManager(adminProvider);

        http
                .authenticationManager(adminAuthManager)
                .securityMatcher("/admin/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/fonts/**", "/uploads/**").permitAll() // 정적 리소스 (작성자: 진원, 2025-11-24) , 이미지 경로 허용(작성자: 윤종인, 2025-11-27)
                        .requestMatchers("/admin/login").permitAll()
                        // 모든 관리 기능: 일반관리자와 최고관리자만 (작성자: 진원, 2025-11-24)
                        .anyRequest().hasAnyAuthority("최고관리자", "일반관리자")
                )
                .formLogin(form -> form
                        .loginPage("/admin/login")
                        .usernameParameter("loginId")
                        .passwordParameter("password")
                        .successHandler(customAuthenticationSuccessHandler)
                        .failureHandler(customAuthenticationFailureHandler)
                )
                .logout(logout -> logout
                        .logoutUrl("/admin/logout")
                        .addLogoutHandler(customLogoutHandler) // 로그인 시간 포인트 세션 제거 (작성자: 진원, 2025-12-04)
                        .invalidateHttpSession(true)
                        .logoutSuccessUrl("/admin/login?logout=true")
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(customAccessDeniedHandler)
                )
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
    @Bean
    @Order(2)
    public SecurityFilterChain memberSecurity(HttpSecurity http) throws Exception {
        DaoAuthenticationProvider memberProvider = new DaoAuthenticationProvider();
        memberProvider.setUserDetailsService(myUserDetailsService);
        memberProvider.setPasswordEncoder(passwordEncoder());
        AuthenticationManager memberAuthManager = new ProviderManager(memberProvider);

        http
                .authenticationManager(memberAuthManager)
                .securityMatcher("/member/**", "/my/**", "/cs/customerSupport/login/**", "/quiz/**", "/api/quiz/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/fonts/**", "/uploads/**").permitAll() // 정적 리소스 (작성자: 진원, 2025-11-24)
                        .requestMatchers("/member/**").permitAll()
                        .requestMatchers("/quiz/**").permitAll() // 퀴즈 페이지 접근 허용 (작성자: 진원, 2025-11-24)
                        .requestMatchers("/api/quiz/ranking").permitAll() // 랭킹 API 공개 (작성자: 진원, 2025-11-25)
                        .requestMatchers("/api/quiz/**").hasRole("USER") // 퀴즈 API는 로그인 필요 (작성자: 진원, 2025-11-24)
                        .requestMatchers("/my/**").hasRole("USER")
                        .requestMatchers("/cs/chat/**").hasRole("CONSULTANT")// 상담원
                        .requestMatchers("/cs/customerSupport/login/**").hasRole("USER")

                )
                .formLogin(form -> form
                        .loginPage("/member/login")
                        .usernameParameter("userId")
                        .passwordParameter("userPw")
                        .successHandler(successHandler)
                         /* 2025/12/01 - 회원 상태에 따른 로그인 실패 처리 위해 handler 추가 - 오서정 */
                        .failureHandler(userAuthenticationFailureHandler)
//                        .failureUrl("/member/login?error=true")
                )
                .logout(logout -> logout
                        .logoutUrl("/member/logout")
                        .addLogoutHandler(customLogoutHandler) // 로그인 시간 포인트 세션 제거 (작성자: 진원, 2025-12-04)
                        .invalidateHttpSession(true)
                        .logoutSuccessUrl("/member/login?logout=true")
                ).sessionManagement(session -> session
                        .maximumSessions(1)
                        .expiredUrl("/member/login?expired=true")
                )
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain commonSecurity(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/fonts/**", "/uploads/**").permitAll() // 정적 리소스 (작성자: 진원, 2025-11-24)
                        .anyRequest().permitAll())
                .csrf(csrf -> csrf.disable());

        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

}