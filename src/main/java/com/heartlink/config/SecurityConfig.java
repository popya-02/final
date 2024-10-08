package com.heartlink.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    // 비밀번호 암호화를 위한 BCryptPasswordEncoder 빈 등록
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // CORS 설정을 위한 CorsFilter 빈 등록
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true); // 자격 증명 허용
        config.addAllowedOriginPattern("*"); // 모든 도메인 허용
        config.addAllowedHeader("*"); // 모든 헤더 허용
        config.addAllowedMethod("*"); // 모든 HTTP 메소드 허용

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    // Spring Security의 필터 체인 설정
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // CSRF 보호 비활성화
                .cors(cors -> cors.configurationSource(request -> new CorsConfiguration().applyPermitDefaultValues()))
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                // 예: 어드민만 접근 가능한 URL 설정
                                .requestMatchers("/admin/**", "/notices/new", "/notices/edit", "/notices/delete", "/notices/delete").hasRole("ADMIN")
                                // 일부 페이지만 접속 권한 설정
                                .requestMatchers("/matching/mbti").authenticated()    //은식
                                .requestMatchers("/mypage/**", "/review/photoenroll").authenticated()    //아태
                                .requestMatchers("/matching-area/**", "/matching/**").authenticated()    //재인

                                // 그 외의 모든 요청은 권한 허용
                                .requestMatchers("/ws/**").permitAll()
                                .anyRequest().permitAll()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션을 사용하지 않음 (JWT 사용)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((request, response, authException) -> {
                            // 로그인되지 않은 사용자가 접근 시 리다이렉션 URL 설정
                            response.sendRedirect("/member/sign?error=unauthorized");
                        })
                )
                .logout(logout ->
                        logout
                                .logoutUrl("/member/logout")  // 로그아웃 URL 설정
                                .logoutSuccessUrl("/member/sign")  // 로그아웃 성공 시 리디렉션할 URL
                                .invalidateHttpSession(true)  // 세션 무효화
                                .deleteCookies("token", "adminToken")
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class); // JwtFilter를 UsernamePasswordAuthenticationFilter 이전에 추가
        return http.build();
    }
}
