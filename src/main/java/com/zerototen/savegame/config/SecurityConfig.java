package com.zerototen.savegame.config;

import com.zerototen.savegame.exception.AccessDeniedHandlerException;
import com.zerototen.savegame.exception.AuthenticationEntryPointException;
import com.zerototen.savegame.security.TokenProvider;
import com.zerototen.savegame.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.ConditionalOnDefaultWebSecurity;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@ConditionalOnDefaultWebSecurity
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SecurityConfig {

    @Value("${jwt.secret}")
    String SECRET_KEY;
    private final TokenProvider tokenProvider;
    private final UserDetailsServiceImpl userDetailsService;
    private final AuthenticationEntryPointException authenticationEntryPointException;
    private final AccessDeniedHandlerException accessDeniedHandlerException;

//  @Bean
//  public WebSecurityCustomizer webSecurityCustomizer() {
//    // h2-console 사용에 대한 허용 (CSRF, FrameOptions 무시)
//    return (web) -> web.ignoring()
//            .antMatchers("/h2-console/**");
//  }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Order(SecurityProperties.BASIC_AUTH_ORDER)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()

//            .headers().frameOptions().sameOrigin()
            .cors().configurationSource(corsConfigurationSource())

            .and()
            .exceptionHandling()
            .authenticationEntryPoint(authenticationEntryPointException)
            .accessDeniedHandler(accessDeniedHandlerException)

            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

            .and()
            .authorizeRequests()
            .antMatchers(HttpMethod.OPTIONS, "/**/*").permitAll()
            .antMatchers("/api/auth/**").permitAll()
            .antMatchers("/sub/**").permitAll()
            .antMatchers("/pub/**").permitAll()
            .antMatchers("/ws-stomp/**").permitAll()
            .antMatchers("/h2-console/**").permitAll()
            .antMatchers("/index").permitAll()
//            .anyRequest().authenticated()
            .anyRequest().permitAll()  // TODO: 임시로 모든 접근 허용

            .and()
            .apply(new JwtSecurityConfig(SECRET_KEY, tokenProvider, userDetailsService));

        return http.build();
    }

    // CORS 허용 적용
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.addAllowedOrigin("*");
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}