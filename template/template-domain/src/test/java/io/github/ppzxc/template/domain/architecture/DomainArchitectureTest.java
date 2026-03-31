package io.github.ppzxc.template.domain.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
    packages = "io.github.ppzxc.template.domain",
    importOptions = ImportOption.DoNotIncludeTests.class)
class DomainArchitectureTest {

  @ArchTest
  static final ArchRule noSpringDependency =
      noClasses()
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("org.springframework..")
          .allowEmptyShould(true)
          .as("도메인 레이어는 Spring에 의존할 수 없다");

  @ArchTest
  static final ArchRule noJpaDependency =
      noClasses()
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("jakarta.persistence..")
          .allowEmptyShould(true)
          .as("도메인 레이어는 JPA에 의존할 수 없다");

  @ArchTest
  static final ArchRule onlyAllowedDependencies =
      noClasses()
          .should()
          .dependOnClassesThat()
          .resideOutsideOfPackages(
              "io.github.ppzxc.template.domain..",
              "java..",
              "javax..",
              "jakarta.validation..",
              "lombok..",
              "org.jspecify..")
          .allowEmptyShould(true)
          .as("도메인 레이어는 허용된 패키지에만 의존할 수 있다");
}
