---
status: accepted
date: 2026-03-31
decision-makers: ppzxc
consulted:
informed:
---

# Adopt Flat Module Layout Under Wrapper Directory

## Context and Problem Statement

A hexagonal architecture project with 11 Gradle modules needs a directory structure.
The existing layout used `template/apps/`, `template/modules/`, `template/libs/` — three
intermediate grouping folders that add unnecessary nesting depth. Module names already
encode their role via prefixes (`template-adapter-*`, `template-boot-*`), making the
intermediate folders redundant. The root directory should remain clean, containing only
build and configuration files.

## Decision Drivers

* Clean root directory — only build/config files at project root
* Self-documenting module names — `template-adapter-input-web` is clear without folder grouping
* Remove redundant nesting — `apps/modules/libs` duplicates information already in module name prefixes
* Industry practice — large open-source projects (Netflix, Axon Framework) use flat module layouts
* IDE navigation efficiency — all modules visible at one level

## Considered Options

* **A. Flat under template/** — keep `template/` wrapper, remove intermediate grouping
* **B. Flat at root** — place all modules at the project root level
* **C. By Hex Layer** — group into `core/`, `adapters/`, `bootstrap/`
* **D. By Role (previous)** — group into `apps/`, `modules/`, `libs/`

## Decision Outcome

Chosen option: "A. Flat under template/", because it keeps the root directory dedicated to
build/config files while placing all modules flat under `template/`. The intermediate
grouping folders are removed, reducing nesting by one level without cluttering the root.
The existing `module()` helper in `settings.gradle.kts` is retained with simplified paths.

### Consequences

* Good, because intermediate folders (apps/modules/libs) are removed, reducing navigation depth by one level
* Good, because root directory stays clean with only build/config files
* Good, because adding a new module requires only creating a directory under `template/` and one line in settings
* Neutral, because `module()` helper is still needed (template/ wrapper requires projectDir mapping)

### Confirmation

Verified by running `./gradlew compileJava` and `./gradlew build` successfully.

## Pros and Cons of the Options

### B. Flat at root

* Good, because `settings.gradle.kts` can use plain `include()`, no custom helper needed
* Bad, because 12 module folders mixed with build files clutters the root directory

### C. By Hex Layer

* Good, because architecture intent is explicitly visible in folder structure
* Bad, because module names and folder names carry duplicate information
* Bad, because nesting depth is similar to the previous structure

### D. By Role (previous structure)

* Good, because apps, core modules, and libraries are visually separated
* Bad, because `apps/modules/libs` grouping duplicates module name prefixes
* Bad, because three levels of nesting slows navigation

## More Information

**Why retain the `module()` helper:**
When modules live under a `template/` wrapper directory, Gradle's standard `include()`
looks for modules at the project root. The `module()` helper sets `projectDir` to map
Gradle module names to their physical location under `template/`. With flat layout,
the path simplifies from `template/modules/template-domain` to `template/template-domain`.
