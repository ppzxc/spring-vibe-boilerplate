package io.github.ppzxc.template.autoconfigure.application;

import org.springframework.boot.autoconfigure.AutoConfiguration;

/**
 * UseCase 구현체를 Spring Bean으로 등록하는 AutoConfiguration.
 *
 * <p>Application 레이어는 Spring 의존이 금지되므로 {@code @Service} 대신 여기서 {@code @Bean}으로 등록한다.
 *
 * <pre>{@code
 * @Bean
 * CreateOrderUseCase createOrderUseCase(SaveOrderPort savePort) {
 *     return new CreateOrderService(savePort);
 * }
 * }</pre>
 */
@AutoConfiguration
public class ApplicationAutoConfiguration {}
