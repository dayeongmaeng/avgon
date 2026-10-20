---
name: avgon-development
description: Analyze, implement, test, or review AvgOn (평단온) product work in this repository while preserving its average-cost, purchase-history, cross-platform, and price-alert invariants. Use for AvgOn feature work, bug fixes, API or data changes, tests, and code reviews; do not use for unrelated repositories or general stock-investing advice.
---

# AvgOn Development

Use this workflow to turn an AvgOn request into a small, verified change without silently inventing product policy.

## Establish the working contract

1. Read the repository-root `AGENTS.md` and any nearer `AGENTS.md` that governs the target files. Treat those files as the canonical source for product invariants and architecture; do not repeat them in the implementation plan.
2. Inspect the repository structure, relevant code and tests, configured build commands, and current changes before editing. Preserve unrelated work.
3. Restate the requested outcome internally as observable behavior. Classify information as:
   - **confirmed**: stated by the user, root instructions, existing API contract, schema, or tests;
   - **inferred**: strongly implied by the current implementation;
   - **unresolved**: requires a product or technical choice not established by the repository.
4. Ask only when an unresolved choice would materially change externally visible behavior, data compatibility, security, or irreversible work. Otherwise choose the smallest reversible option and disclose the assumption.

## Analyze the change

- Trace the complete affected path: route or screen, client state/query, API contract, controller/service/domain logic, persistence, scheduled monitoring, and notification delivery. Inspect only the layers relevant to the request.
- Identify which root invariants the change touches. For portfolio work, explicitly trace the baseline position separately from managed purchase records. For alert work, write the threshold transition and deduplication state before changing code.
- Prefer extending the established feature boundary. Introduce a new abstraction or dependency only when it removes concrete duplication or protects a domain invariant in the current task.
- Plan the smallest tests that would fail before the change and prove the requested behavior afterward.

## Implement

1. Add or update a failing test first when the behavior is testable at the domain or API boundary. For a defect, reproduce the defect with the narrowest reliable test.
2. Implement the smallest coherent slice. Keep calculation and alert-transition rules independent of UI and transport details so they can be tested deterministically.
3. Reuse shared React Native/Web code by default. Add platform-specific behavior only at the narrow integration boundary that needs it.
4. Keep TanStack Query as the owner of server state and Zustand as the owner of non-server shared client state. Do not mirror one source of truth into the other.
5. On the server, keep HTTP mapping, use-case orchestration, domain rules, and persistence responsibilities distinct without adding ceremonial layers.
6. Preserve exact numeric semantics end to end. Never route monetary calculations through binary floating-point; make division and rounding decisions visible at the domain boundary.
7. Treat portfolio recalculation and alert deduplication as consistency-sensitive writes. Use the existing transaction and concurrency mechanisms rather than process-local assumptions.
8. Keep the change scoped. Do not add nearby screens, trade types, providers, background infrastructure, or generalized frameworks unless the request requires them.

## Verify

Run the narrowest relevant checks first, then broader configured checks when practical.

- Calculation changes: unit tests for the formula, baseline/history separation, edit/delete recalculation, zero handling, and decimal edge cases.
- Portfolio API or persistence changes: validation, authorization/ownership, transaction rollback, not-found behavior, and PostgreSQL-relevant integration tests.
- Alert changes: equality, upward crossing, repeated samples without duplicates, re-arming after a drop, provider failure, retries, and concurrent scheduler execution as applicable.
- Client changes: type checks, component or flow tests, query invalidation, loading/error/empty states, and both web and iOS build paths affected by shared code.
- Contract or schema changes: compatibility with existing clients/data and the repository's migration/serialization conventions.

Do not claim checks that were not run. If a check cannot run, capture the exact blocker and use the strongest available lower-level verification.

## Review the result

Before finishing, inspect the diff rather than relying only on passing tests.

- Confirm every changed file is necessary for the request.
- Re-evaluate the root domain invariants, especially duplicate baseline cost, stale aggregates after history edits, numeric precision, ownership enforcement, and duplicate alert races.
- Check that API errors and data representations remain consistent and that secrets or push tokens cannot leak through source or logs.
- Check that shared client code remains platform-neutral and platform branches are isolated.
- Remove dead code, accidental formatting churn, debug output, and speculative TODOs introduced by the change.

When the user requests code review only, do not edit. Report actionable findings ordered by severity with file and line, triggering conditions, impact, and a concise remediation direction. State explicitly when no findings remain, and mention residual test gaps.

## Report

Return a compact handoff containing:

1. the user-visible or domain behavior completed;
2. the important files or layers changed;
3. verification commands run and their outcomes;
4. assumptions, unresolved product decisions, skipped checks, or remaining risks.

Separate confirmed behavior from recommendations. Do not present a proposed choice as an established AvgOn requirement.
