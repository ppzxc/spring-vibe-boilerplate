package io.github.ppzxc.boilerplate.adapter.input.api;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class AdapterInputArchitectureTest {

    @Test
    void inboundAdaptersShouldNotDependOnOutboundPortsOrAdapters() {
        JavaClasses importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("io.github.ppzxc.boilerplate");

        noClasses()
                .that().resideInAPackage("io.github.ppzxc.boilerplate.adapter.input..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "io.github.ppzxc.boilerplate.application.port.output..",
                        "io.github.ppzxc.boilerplate.adapter.output.."
                )
                .check(importedClasses);
    }

    @Test
    void inboundAdaptersShouldOnlyDependOnInputPortsAndDomain() {
        JavaClasses importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("io.github.ppzxc.boilerplate");

        classes()
                .that().resideInAPackage("io.github.ppzxc.boilerplate.adapter.input..")
                .and().areNotInterfaces()
                .and().haveSimpleNameEndingWith("Controller")
                .should().onlyDependOnClassesThat().resideInAnyPackage(
                        "io.github.ppzxc.boilerplate.adapter.input..",
                        "io.github.ppzxc.boilerplate.application.port.input..",
                        "io.github.ppzxc.boilerplate.domain..",
                        "java..",
                        "javax..",
                        "jakarta..",
                        "org.springframework..",
                        "org.slf4j..",
                        "org.mapstruct..",
                        "lombok..",
                        // Record/framework boundaries
                        "io.swagger.v3..",
                        "com.fasterxml.jackson.."
                )
                .check(importedClasses);
    }
}