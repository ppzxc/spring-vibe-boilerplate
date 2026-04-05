package io.github.ppzxc.boilerplate.application.port.output.shared;

/** 도메인 이벤트를 발행하는 Outbound Port. */
public interface PublishEventPort {
  void publish(Object event);
}
