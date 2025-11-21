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
                        .requestMatchers("/admin/login").permitAll()
                        // 상품관리자 권한
                        .requestMatchers("/admin/product/**", "/admin/category/**").hasAnyRole("PRODUCT_ADMIN", "SUPER_ADMIN", "ADMIN")
                        // 회원관리자 권한
                        .requestMatchers("/admin/member/**").hasAnyRole("MEMBER_ADMIN", "SUPER_ADMIN", "ADMIN")
                        // 게시판관리자 권한
                        .requestMatchers("/admin/notice/**", "/admin/faq/**", "/admin/event/**").hasAnyRole("BOARD_ADMIN", "SUPER_ADMIN", "ADMIN")
                        // 고객센터관리자 권한
                        .requestMatchers("/admin/counsel/**", "/admin/report/**").hasAnyRole("CS_ADMIN", "SUPER_ADMIN", "ADMIN")
                        // 모든 관리자 공통: 계정 설정 페이지
                        .requestMatchers("/admin/setting/profile").hasAnyRole("ADMIN", "SUPER_ADMIN", "PRODUCT_ADMIN", "MEMBER_ADMIN", "BOARD_ADMIN", "CS_ADMIN")
                        // 나머지는 최고관리자와 일반관리자만 (약관, 문서 등)
                        .anyRequest().hasAnyRole("SUPER_ADMIN", "ADMIN")
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
                .securityMatcher("/member/**", "/my/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/member/**").permitAll()
                        .requestMatchers("/my/**").hasRole("USER")
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
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable());

        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

}