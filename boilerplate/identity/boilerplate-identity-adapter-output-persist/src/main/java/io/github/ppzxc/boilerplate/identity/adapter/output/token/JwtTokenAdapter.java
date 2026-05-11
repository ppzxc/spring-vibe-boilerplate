package io.github.ppzxc.boilerplate.identity.adapter.output.token;

import io.github.ppzxc.boilerplate.identity.application.port.output.IssueTokenPort;
import io.github.ppzxc.boilerplate.identity.domain.model.AccessToken;
import io.github.ppzxc.boilerplate.identity.domain.model.RefreshToken;
import io.github.ppzxc.boilerplate.identity.domain.model.TokenSet;
import io.github.ppzxc.boilerplate.identity.domain.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenAdapter implements IssueTokenPort {

  private final SecretKey key;
  private final Duration accessTokenTtl;
  private final Duration refreshTokenTtl;

  public JwtTokenAdapter(
      @Value("${security.jwt.hmac-secret}") String hmacSecret,
      @Value("${security.jwt.access-token-ttl:PT15M}") Duration accessTokenTtl,
      @Value("${security.jwt.refresh-token-ttl:P7D}") Duration refreshTokenTtl) {
    this.key = Keys.hmacShaKeyFor(hmacSecret.getBytes(StandardCharsets.UTF_8));
    this.accessTokenTtl = accessTokenTtl;
    this.refreshTokenTtl = refreshTokenTtl;
  }

  @Override
  public TokenSet issue(User user) {
    var now = Instant.now();
    var accessExpiresAt = now.plus(accessTokenTtl);
    var refreshExpiresAt = now.plus(refreshTokenTtl);

    var accessToken =
        Jwts.builder()
            .subject(user.id().value().toString())
            .claim("tid", "default") // TODO: Multi-tenancy 지원 시 확장
            .claim("scope", "") // TODO: User Role/Permission 지원 시 확장
            .issuedAt(Date.from(now))
            .expiration(Date.from(accessExpiresAt))
            .signWith(key)
            .compact();

    var refreshTokenValue = UUID.randomUUID().toString(); // Opaque refresh token

    return new TokenSet(
        new AccessToken(accessToken), new RefreshToken(refreshTokenValue, refreshExpiresAt));
  }
}
