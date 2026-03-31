package io.github.ppzxc.template.adapter.input.web.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 모든 HTTP 응답에 Api-Version 헤더를 추가하는 필터.
 *
 * <p>Deprecation (RFC 9745), Sunset (RFC 8594) 헤더는 설정값이 있을 때만 추가.
 */
public class ApiVersionFilter extends OncePerRequestFilter {

  private final ApiVersionProperties properties;

  public ApiVersionFilter(ApiVersionProperties properties) {
    this.properties = properties;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    response.setHeader("Api-Version", properties.version());

    if (properties.deprecation() != null) {
      response.setHeader("Deprecation", properties.deprecation());
    }
    if (properties.sunset() != null) {
      response.setHeader("Sunset", properties.sunset());
    }

    filterChain.doFilter(request, response);
  }
}
