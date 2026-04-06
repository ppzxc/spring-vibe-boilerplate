package io.github.ppzxc.boilerplate.dummy;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DummyAdapter implements SaveDummyPort {
  private final JdbcTemplate jdbcTemplate;

  public DummyAdapter(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void save(DummyDomain domain) {
    jdbcTemplate.update(
        "INSERT INTO dummy_table (id, name) VALUES (?, ?)", domain.id(), domain.name());
  }

  @Override
  public int count() {
    Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM dummy_table", Integer.class);
    return count != null ? count : 0;
  }

  @Override
  public void clear() {
    jdbcTemplate.execute("DELETE FROM dummy_table");
  }
}
