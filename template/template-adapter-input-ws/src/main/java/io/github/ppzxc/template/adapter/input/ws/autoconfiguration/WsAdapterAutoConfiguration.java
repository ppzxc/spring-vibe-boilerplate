package io.github.ppzxc.template.adapter.input.ws.autoconfiguration;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * template-adapter-input-ws의 WebSocket 컴포넌트를 자동 등록.
 *
 * <p>WebSocket은 경량 알림 채널 전용. 실제 데이터 전송은 HTTP API 사용.
 */
@AutoConfiguration
@ComponentScan(basePackages = "io.github.ppzxc.template.adapter.input.ws")
public class WsAdapterAutoConfiguration {}
