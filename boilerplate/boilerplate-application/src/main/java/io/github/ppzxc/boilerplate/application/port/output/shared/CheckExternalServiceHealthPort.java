package io.github.ppzxc.boilerplate.application.port.output.shared;

/** 외부 서비스 연결 상태를 확인하는 Outbound Port. */
public interface CheckExternalServiceHealthPort {

  /** 외부 서비스가 정상인지 확인한다. */
  boolean isHealthy();
}
