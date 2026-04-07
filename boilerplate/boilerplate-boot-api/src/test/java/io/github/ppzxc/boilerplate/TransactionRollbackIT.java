package io.github.ppzxc.boilerplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.ppzxc.boilerplate.dummy.DummyAdapter;
import io.github.ppzxc.boilerplate.dummy.SaveDummyPort;
import io.github.ppzxc.boilerplate.dummy.TriggerExceptionService;
import io.github.ppzxc.boilerplate.dummy.TriggerExceptionUseCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Import(TransactionRollbackIT.TestConfig.class)
class TransactionRollbackIT {

  @TestConfiguration
  @EnableTransactionManagement
  static class TestConfig {
    @Bean
    @Transactional
    public TriggerExceptionUseCase triggerExceptionUseCase(SaveDummyPort port) {
      return new TriggerExceptionService(port);
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
