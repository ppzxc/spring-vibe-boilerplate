package io.github.ppzxc.boilerplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.ppzxc.boilerplate.dummy.DummyAdapter;
import io.github.ppzxc.boilerplate.dummy.SaveDummyPort;
import io.github.ppzxc.boilerplate.dummy.TriggerExceptionService;
import io.github.ppzxc.boilerplate.dummy.TriggerExceptionUseCase;
import java.util.Properties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(
    properties =
        "spring.autoconfigure.exclude="
            + "io.github.springwolf.core.configuration.SpringwolfAutoConfiguration,"
            + "io.github.springwolf.plugins.stomp.configuration.SpringwolfStompAutoConfiguration,"
            + "io.github.springwolf.bindings.stomp.configuration.SpringwolfStompBindingAutoConfiguration")
@Import(TransactionRollbackIT.TestConfig.class)
@Testcontainers
class TransactionRollbackIT {

  @Container static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
    registry.add("spring.jooq.sql-dialect", () -> "POSTGRES");
  }

  @TestConfiguration
  static class TestConfig {
    @Bean
    public TriggerExceptionUseCase triggerExceptionUseCase(
        SaveDummyPort port, PlatformTransactionManager txManager) {
      TransactionInterceptor interceptor = new TransactionInterceptor();
      interceptor.setTransactionManager(txManager);
      Properties attrs = new Properties();
      attrs.setProperty("*", "PROPAGATION_REQUIRED");
      interceptor.setTransactionAttributes(attrs);

      ProxyFactory factory = new ProxyFactory(new TriggerExceptionService(port));
      factory.addInterface(TriggerExceptionUseCase.class);
      factory.addAdvice(interceptor);
      return TriggerExceptionUseCase.class.cast(factory.getProxy());
    }
  }

  @Autowired private TriggerExceptionUseCase triggerExceptionUseCase;

  @Autowired private DummyAdapter dummyAdapter;

  @Autowired private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void setUp() {
    jdbcTemplate.execute(
        "CREATE TABLE IF NOT EXISTS dummy_table (id BIGINT PRIMARY KEY, name VARCHAR(255))");
    dummyAdapter.clear();
  }

  @AfterEach
  void tearDown() {
    jdbcTemplate.execute("DROP TABLE IF EXISTS dummy_table");
  }

  @Test
  void shouldRollbackTransactionWhenExceptionIsThrown() {
    // Given
    assertThat(dummyAdapter.count()).isZero();

    // When
    assertThrows(RuntimeException.class, () -> triggerExceptionUseCase.executeWithException());

    // Then
    // If the @Transactional proxy works, the first insert should have been rolled back.
    assertThat(dummyAdapter.count()).isZero();
  }
}
