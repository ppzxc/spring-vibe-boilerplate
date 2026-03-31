package io.github.ppzxc.template.application.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * template-application 아키텍처 규칙 검증.
 *
 * <p>애플리케이션 레이어는 Spring에 의존할 수 없으며, 순수 Java UseCase와 Port 인터페이스로만 구성된다.
 *
 * <p>{@code allowEmptyShould(true)}: 초기 빈 상태에서도 통과. 위반 코드가 추가되면 즉시 감지된다.
 */
@AnalyzeClasses(packages = "io.github.ppzxc.template.application")
class ApplicationArchitectureTest {

  @ArchTest
  static final ArchRule no_spring_dependencies =
      noClasses()
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("org.springframework..")
          .allowEmptyShould(true)
          .as("애플리케이션 레이어는 Spring에 의존할 수 없다");

  @ArchTest
  static final ArchRule outbound_ports_are_interfaces =
      classes()
          .that()
          .resideInAPackage("..port.out..")
          .should()
          .beInterfaces()
          .allowEmptyShould(true)
          .as("Outbound Port는 반드시 인터페이스여야 한다");

  @ArchTest
  static final ArchRule application_only_depends_on_allowed_packages =
      classes()
          .should()
          .onlyDependOnClassesThat()
          .resideInAnyPackage(
              "io.github.ppzxc.template.application..",
              "io.github.ppzxc.template.domain..",
              "io.github.ppzxc.template.common..",
              "java..",
              "javax..")
          .allowEmptyShould(true)
          .as("애플리케이션 레이어는 domain과 common에만 의존할 수 있다");

  @ArchTest
  static final ArchRule query_services_do_not_use_command_ports =
      noClasses()
          .that()
          .resideInAPackage("..service.query..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..port.out.command..")
          .allowEmptyShould(true)
          .as("Query 서비스는 Command 포트(Save*)에 의존할 수 없다");

  @ArchTest
  static final ArchRule command_use_cases_are_interfaces =
      classes()
          .that()
          .resideInAPackage("..port.in.command..")
          .and()
          .haveSimpleNameEndingWith("UseCase")
          .should()
          .beInterfaces()
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule query_use_cases_are_interfaces =
      classes()
          .that()
          .resideInAPackage("..port.in.query..")
          .and()
          .haveSimpleNameEndingWith("Query")
          .should()
          .beInterfaces()
          .allowEmptyShould(true);
}
