package io.github.ppzxc.boilerplate.test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/** Domain 단위 테스트 공통 기반. Spring import 없음. */
public abstract class DomainTestBase {

  protected static final Instant NOW = Instant.parse("2026-01-15T10:00:00Z");
  protected static final Instant LATER = NOW.plus(Duration.ofHours(1));

  /** 결정적 UUIDv7 픽스처. epochMillis 기반으로 MSB를 고정하고 LSB는 카운터로 채움. */
  protected static UUID fixedUuidV7(long epochMillis) {
    long msb = ((epochMillis & 0x0000_FFFF_FFFF_FFFFL) << 16) | 0x7000L | 0x0001L;
    long lsb = 0x8000_0000_0000_0001L;
    return new UUID(msb, lsb);
  }
}
