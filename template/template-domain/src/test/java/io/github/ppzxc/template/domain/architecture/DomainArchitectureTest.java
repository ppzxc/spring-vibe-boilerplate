package io.github.ppzxc.template.domain.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.Location;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import java.util.regex.Pattern;

@AnalyzeClasses(
    packages = "io.github.ppzxc.template.domain",
    importOptions = {
      ImportOption.DoNotIncludeTests.class,
      DomainArchitectureTest.DoNotIncludeTestFixtures.class
    })
class DomainArchitectureTest {

  static final class DoNotIncludeTestFixtures implements ImportOption {
    // Matches both Gradle directory (testFixtures) and JAR artifact (test-fixtures)
    private static final Pattern PATTERN = Pattern.compile(".*(testFixtures|test-fixtures).*");

    @Override
    public boolean includes(Location location) {
      return !location.matches(PATTERN);
    }
  }

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
