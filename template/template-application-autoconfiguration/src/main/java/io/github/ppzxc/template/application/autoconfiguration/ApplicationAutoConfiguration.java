package io.github.ppzxc.template.application.autoconfiguration;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/** UseCase 구현체를 Spring Bean으로 등록하는 AutoConfiguration. */
@AutoConfiguration
@ComponentScan(basePackages = "io.github.ppzxc.template.application")
public class ApplicationAutoConfiguration {}
