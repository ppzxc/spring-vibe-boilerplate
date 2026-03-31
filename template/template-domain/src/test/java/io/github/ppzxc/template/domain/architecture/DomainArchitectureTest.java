package io.github.ppzxc.template.domain.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * template-domain 아키텍처 규칙 검증.
 *
 * <p>도메인 레이어는 Spring, JPA 어노테이션을 사용할 수 없으며, template-common, 표준 Java 라이브러리, Bean Validation,
 * Lombok에만 의존할 수 있다.
 *
 * <p>{@code allowEmptyShould(true)}: 도메인 클래스가 없는 초기 상태에서도 통과. 위반 클래스가 추가되면 즉시 감지된다.
 */
@AnalyzeClasses(packages = "io.github.ppzxc.template.domain")
class DomainArchitectureTest {

  @ArchTest
  static final ArchRule no_spring_dependencies =
      noClasses()
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("org.springframework..")
          .allowEmptyShould(true)
          .as("도메인 레이어는 Spring에 의존할 수 없다");

  @ArchTest
  static final ArchRule no_jpa_dependencies =
      noClasses()
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("jakarta.persistence..")
          .allowEmptyShould(true)
          .as("도메인 레이어는 JPA에 의존할 수 없다");

  @ArchTest
  static final ArchRule domain_only_depends_on_allowed_packages =
      classes()
          .should()
          .onlyDependOnClassesThat()
          .resideInAnyPackage(
              "io.github.ppzxc.template.domain..",
              "io.github.ppzxc.template.common..",
              "java..",
              "javax..",
              "jakarta.validation..",
              "lombok..")
          .allowEmptyShould(true)
          .as("도메인 레이어는 허용된 패키지에만 의존할 수 있다");
}
