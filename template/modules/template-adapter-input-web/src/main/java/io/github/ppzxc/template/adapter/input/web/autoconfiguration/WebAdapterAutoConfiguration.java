package io.github.ppzxc.template.adapter.input.web.autoconfiguration;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * template-adapter-input-web의 Spring 컴포넌트를 자동 등록.
 *
 * <p>@RestController, Security Filter 등 io.github.ppzxc.template.adapter.input.web 패키지 하위 모든 컴포넌트를
 * 스캔.
 */
@AutoConfiguration
@ComponentScan(basePackages = "io.github.ppzxc.template.adapter.input.web")
public class WebAdapterAutoConfiguration {}
