package io.github.ppzxc.boilerplate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;

/** Spring Boot API 애플리케이션 진입점. */
// sharedModules = "shared.event" 은 boilerplate-shared-event 모듈 추가 시 등록
@Modulithic(systemName = "Boilerplate")
@SpringBootApplication
public class BoilerplateApplication {

  public static void main(String[] args) {
    SpringApplication.run(BoilerplateApplication.class, args);
  }
}
