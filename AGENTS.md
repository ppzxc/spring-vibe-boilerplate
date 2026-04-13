# Project Instructions

This project uses `.claude/` for AI agent configuration.

- Project context: [.claude/CLAUDE.md](.claude/CLAUDE.md)
- Coding rules: `.claude/rules/*.md`
- Decision records: `docs/decisions/*.md`

If your tool supports reading these paths, follow them.

Otherwise, the essential constraints are:

1. **Domain zero dependencies**: `boilerplate-*-domain` module MUST have zero external dependencies. No Spring, no JPA, no logging frameworks, no Jakarta annotations.
2. **Application depends on domain only**: `boilerplate-*-application` MUST NOT depend on Spring, jOOQ, or any persistence framework.
3. **All business logic in domain**: if/else business branching MUST live inside Entity or Domain Service methods — not in Application Services.
4. **1 transaction = 1 Aggregate**: A single UseCase invocation MUST only persist one Aggregate Root. Cross-Aggregate coordination uses Domain Events.
5. **No @Transactional in application**: Transaction management is handled via Configuration-layer TX proxy, never via @Transactional annotation on Application Services.
