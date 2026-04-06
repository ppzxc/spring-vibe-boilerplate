package io.github.ppzxc.boilerplate.adapter.output.persist;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class AdapterOutputArchitectureTest {

    @Test
    void outboundAdaptersShouldNotDependOnInputPortsOrAdapters() {
        JavaClasses importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("io.github.ppzxc.boilerplate");

        noClasses()
                .that().resideInAPackage("io.github.ppzxc.boilerplate.adapter.output..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "io.github.ppzxc.boilerplate.application.port.input..",
                        "io.github.ppzxc.boilerplate.adapter.input.."
                )
                .check(importedClasses);
    }

    @Test
    void outboundAdaptersShouldImplementOutputPorts() {
        JavaClasses importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("io.github.ppzxc.boilerplate");

        classes()
                .that().resideInAPackage("io.github.ppzxc.boilerplate.adapter.output..")
                .and().haveSimpleNameEndingWith("Adapter")
                .and().areNotInterfaces()
                .should().implement(com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage("io.github.ppzxc.boilerplate.application.port.output.."))
                .check(importedClasses);
    }
}