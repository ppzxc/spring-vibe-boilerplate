package io.github.ppzxc.boilerplate.application.port.output.shared;

import java.util.Optional;

/** 외부 데이터 조회 Outbound Port. 구현체는 adapter-output-external 모듈에 위치. */
public interface ExternalDataPort {

  Optional<String> fetchById(String id);
}
