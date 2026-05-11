package io.github.ppzxc.boilerplate.identity.adapter.output.persist;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import io.github.ppzxc.boilerplate.identity.application.port.output.SaveRefreshTokenPort;
import io.github.ppzxc.boilerplate.identity.domain.model.RefreshToken;
import io.github.ppzxc.boilerplate.identity.domain.model.UserId;
import java.time.ZoneOffset;
import java.util.Objects;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenPersistenceAdapter implements SaveRefreshTokenPort {

  private final DSLContext dsl;

  public RefreshTokenPersistenceAdapter(DSLContext dsl) {
    this.dsl = Objects.requireNonNull(dsl, "dsl must not be null");
  }

  @Override
  public void save(UserId userId, RefreshToken refreshToken) {
    // Jooq 클래스 생성 전이므로 String 기반으로 우선 작성 (나중에 Jooq 클래스로 리팩토링 가능)
    dsl.insertInto(table("refresh_tokens"))
        .set(field("token"), refreshToken.value())
        .set(field("user_id"), userId.value())
        .set(field("expires_at"), refreshToken.expiresAt().atOffset(ZoneOffset.UTC))
        .set(field("created_at"), java.time.OffsetDateTime.now(ZoneOffset.UTC))
        .execute();
  }
}
