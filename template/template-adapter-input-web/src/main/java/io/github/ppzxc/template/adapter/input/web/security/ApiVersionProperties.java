package io.github.ppzxc.template.adapter.input.web.security;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Api-Version 관련 설정값.
 *
 * @param version 현재 API 버전 (ISO 8601 날짜, 예: 2026-01-01)
 * @param deprecation Deprecation 헤더 값 (RFC 9745, 설정 안 하면 null)
 * @param sunset Sunset 헤더 값 (RFC 8594, 설정 안 하면 null)
 */
@ConfigurationProperties(prefix = "template.api")
public record ApiVersionProperties(
    String version, @Nullable String deprecation, @Nullable String sunset) {}
