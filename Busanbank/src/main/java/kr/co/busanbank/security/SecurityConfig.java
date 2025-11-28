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

    @Autowired
    private AdminUserDetailsService adminUserDetailsService;

    @Autowired
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @Autowired
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Autowired
    private CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    private final CustomLoginSuccessHandler successHandler = new CustomLoginSuccessHandler();
    private final AdminLoginSuccessHandler adminSuccessHandler = new AdminLoginSuccessHandler();
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
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/fonts/**").permitAll() // 정적 리소스 (작성자: 진원, 2025-11-24)
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
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/fonts/**").permitAll() // 정적 리소스 (작성자: 진원, 2025-11-24)
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
                        .failureUrl("/member/login?error=true")
                )
                .logout(logout -> logout
                        .logoutUrl("/member/logout")
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
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/fonts/**").permitAll() // 정적 리소스 (작성자: 진원, 2025-11-24)
                        .anyRequest().permitAll())
                .csrf(csrf -> csrf.disable());

        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

}