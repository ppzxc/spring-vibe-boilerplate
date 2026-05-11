package io.github.ppzxc.boilerplate.identity.adapter.input.api;

import io.github.ppzxc.boilerplate.identity.adapter.input.api.dto.AuthResponse;
import io.github.ppzxc.boilerplate.identity.adapter.input.api.dto.LoginRequest;
import io.github.ppzxc.boilerplate.identity.application.dto.LoginCommand;
import io.github.ppzxc.boilerplate.identity.application.port.input.LoginUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Identity BC Auth REST Controller (AD-1). */
@RestController
@RequestMapping("/api/identity/auth")
public class AuthController {

  private final LoginUseCase loginUseCase;

  public AuthController(LoginUseCase loginUseCase) {
    this.loginUseCase = loginUseCase;
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    var result = loginUseCase.execute(new LoginCommand(request.email(), request.password()));
    return ResponseEntity.ok(
        new AuthResponse(result.accessToken(), result.refreshToken(), result.expiresIn()));
  }
}
