package io.github.ppzxc.template.application.autoconfiguration;

import org.springframework.boot.autoconfigure.AutoConfiguration;

/**
 * UseCase 구현체를 Spring Bean으로 등록하는 AutoConfiguration.
 *
 * <p>template-application 모듈의 UseCase 구현체들은 순수 Java이므로 @Service 어노테이션 없음. 이 클래스에서 @Bean으로 명시적 등록.
 *
 * <p>사용 예시:
 *
 * <pre>{@code
 * @Bean
 * public MyUseCase myUseCase(MyOutPort myOutPort) {
 *   return new MyUseCaseService(myOutPort);
 * }
 * }</pre>
 */
@AutoConfiguration
public class ApplicationAutoConfiguration {}
