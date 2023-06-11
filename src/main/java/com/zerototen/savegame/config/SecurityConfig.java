package com.zerototen.savegame.config;

import com.zerototen.savegame.config.Jwt.JwtAccessDeniedHandler;
import com.zerototen.savegame.config.Jwt.JwtAuthenticationEntryPoint;
import com.zerototen.savegame.config.Jwt.JwtSecurityConfig;
import com.zerototen.savegame.config.Jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {
  private final RedisDao redisDao;
  private final TokenProvider tokenProvider;
  private final CorsFilter corsFilter;
  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
  private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Override
  protected void configure(HttpSecurity httpSecurity) throws Exception {
    httpSecurity
        .csrf().disable()

        .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling()
        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
        .accessDeniedHandler(jwtAccessDeniedHandler)

        .and()
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

        .and()
        .authorizeRequests()
        .antMatchers(
            //일반 로그인
            "/app/sign-up", "/app/auth/login",

            // OAth2
            "/auth/google/**",

            //Swagger
            "/v2/api-docs",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui.html",
            "/webjars/**",
            "/v3/api-docs/**",
            "/swagger-ui/**"
        )
        .permitAll()
        .anyRequest().authenticated()

        .and()
        .apply(new JwtSecurityConfig(tokenProvider, redisDao));
  }
}
