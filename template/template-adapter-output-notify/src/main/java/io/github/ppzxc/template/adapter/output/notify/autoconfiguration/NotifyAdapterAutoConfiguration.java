package io.github.ppzxc.template.adapter.output.notify.autoconfiguration;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/** template-adapter-output-notify의 이메일/Push 알림 구현체를 자동 등록. */
@AutoConfiguration
@ComponentScan(basePackages = "io.github.ppzxc.template.adapter.output.notify")
public class NotifyAdapterAutoConfiguration {}
