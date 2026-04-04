package io.github.ppzxc.boilerplate.application.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
    packages = "io.github.ppzxc.boilerplate.application",
    importOptions = ImportOption.DoNotIncludeTests.class)
class ApplicationArchitectureTest {

  @ArchTest
  static final ArchRule noSpringDependency =
      noClasses()
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("org.springframework..")
          .allowEmptyShould(true)
          .as("애플리케이션 레이어는 Spring에 의존할 수 없다");

  @ArchTest
  static final ArchRule onlyAllowedDependencies =
      noClasses()
          .should()
          .dependOnClassesThat()
          .resideOutsideOfPackages(
              "io.github.ppzxc.boilerplate.application..",
              "io.github.ppzxc.boilerplate.domain..",
              "java..",
              "javax..",
              "jakarta.validation..",
              "lombok..",
              "org.jspecify..")
          .allowEmptyShould(true)
          .as("애플리케이션 레이어는 허용된 패키지에만 의존할 수 있다");

  @ArchTest
  static final ArchRule outboundPortsMustBeInterfaces =
      classes()
          .that()
          .resideInAPackage("..port.output..")
          .should()
          .beInterfaces()
          .allowEmptyShould(true)
          .as("Outbound Port는 반드시 인터페이스여야 한다");

  @ArchTest
  static final ArchRule commandUseCasesMustBeInterfaces =
      classes()
          .that()
          .resideInAPackage("..port.input.command..")
          .and()
          .haveSimpleNameEndingWith("UseCase")
          .should()
          .beInterfaces()
          .allowEmptyShould(true)
          .as("Inbound Command Port (*UseCase)는 반드시 인터페이스여야 한다");

  @ArchTest
  static final ArchRule queryPortsMustBeInterfaces =
      classes()
          .that()
          .resideInAPackage("..port.input.query..")
          .and()
          .haveSimpleNameEndingWith("Query")
          .should()
          .beInterfaces()
          .allowEmptyShould(true)
          .as("Inbound Query Port (*Query)는 반드시 인터페이스여야 한다");

  @ArchTest
  static final ArchRule queryServicesMustNotDependOnCommandPorts =
      noClasses()
          .that()
          .resideInAPackage("..service.query..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..port.output.command..")
          .allowEmptyShould(true)
          .as("Query 서비스는 Command Outbound Port에 의존할 수 없다");
}
