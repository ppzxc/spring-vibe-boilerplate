package io.github.ppzxc.boilerplate.autoconfigure.adapter.output.persist;

import io.github.ppzxc.boilerplate.adapter.output.persist.TodoPersistAdapter;
import org.jooq.DSLContext;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/** Registers persistence adapter beans backed by jOOQ. */
@AutoConfiguration
public class PersistAdapterAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  TodoPersistAdapter todoPersistAdapter(DSLContext dslContext) {
    return new TodoPersistAdapter(dslContext);
  }
}
