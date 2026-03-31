package io.github.ppzxc.template.adapter.input.web.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;

/**
 * 기본 Security 설정.
 *
 * <p>보일러플레이트 기본값: 모든 요청 허용 (stateless, CSRF 비활성화). 실제 프로젝트에서는 인증 필터와 경로별 권한 설정을 추가한다.
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(ApiVersionProperties.class)
public class SecurityConfig {

  private final ApiVersionProperties apiVersionProperties;

  public SecurityConfig(ApiVersionProperties apiVersionProperties) {
    this.apiVersionProperties = apiVersionProperties;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .addFilterAfter(
            new ApiVersionFilter(apiVersionProperties), SecurityContextHolderFilter.class)
        .build();
  }
}
