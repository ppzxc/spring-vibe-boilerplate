package io.github.ppzxc.boilerplate.autoconfigure;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * CORS 관련 설정 프로퍼티.
 * 앱 기동 시 유효성을 검증한다.
 */
@Validated
@ConfigurationProperties(prefix = "cors")
public record CorsProperties(
    @NotBlank(message = "CORS 허용 오리진은 비어있을 수 없습니다. 기본값 '*'를 사용하거나 명시하세요.")
    String allowedOrigins
) {
}
