package io.github.ppzxc.boilerplate.shared;

public interface AuthorizationPolicy {

  void checkPermission(String resourceScope);
}
