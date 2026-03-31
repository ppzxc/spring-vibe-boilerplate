package io.github.ppzxc.template.adapter.output.channel.autoconfiguration;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * template-adapter-output-channel의 ChannelPort 구현체를 자동 등록.
 *
 * <p>Phase 1: SMS 구현체. Phase 2+: 카카오톡, 웹챗 등 추가 채널 구현체 등록.
 */
@AutoConfiguration
@ComponentScan(basePackages = "io.github.ppzxc.template.adapter.output.channel")
public class ChannelAdapterAutoConfiguration {}
