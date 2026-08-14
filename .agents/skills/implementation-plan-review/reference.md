# Implementation plan review artifact template

Use this template when writing the review artifact. Path and naming rules live in [SKILL.md](SKILL.md).

```markdown
# Implementation Plan Review: <plan title>

> **Plan:** `<plan path>`
> **Review scope:** <plan path and inspected module/docs paths>
> **Verdict:** Pass | Changes required | Blocked

## Summary

<Concise conclusion and the most important evidence.>

## Findings

### <Severity>: <finding title>

- **Location:** `<path>:<line or range>`
- **Planning requirement:** <execution-unit rule, section, acceptance criterion, or dependency rule>
- **Impact:** <why this matters>
- **Recommendation:** <specific correction>

<Repeat in descending severity. Write "No actionable findings." when none exist.>

## Contract coverage

- [Pass | Fail | Partial | Not verified] Goal
  - Evidence: <paths, lines, or explanation>
- [Pass | Fail | Partial | Not verified] Execution unit
  - Evidence: <outcome coherence, one-context implementability/reviewability, and any genuine execution/review boundaries; numeric size heuristics are signals only>
- [Pass | Fail | Partial | Not verified] Research and constraints
  - Evidence: <sources and implementation consequences>
- [Pass | Fail | Partial | Not verified] Deliverables
  - Evidence: <…>
- [Pass | Fail | Partial | Not verified] Non-goals
  - Evidence: <…>
- [Pass | Fail | Partial | Not verified] Implementation boundaries
  - Evidence: <…>
- [Pass | Fail | Partial | Not verified] Test strategy
  - Evidence: <…>
- [Pass | Fail | Partial | Not verified] Acceptance criteria
  - Evidence: <…>
- [Pass | Fail | Partial | Not verified] Dependencies
  - Evidence: <relative Markdown links to surviving live plans, or None>
- [Pass | Fail | Partial | Not verified] Lifecycle
  - Evidence: <Not started | In progress; no Complete; no umbrella original>
- [Pass | Fail | Partial | Not applicable] Nullness / `module-docs` hooks
  - Evidence: <why required or why not applicable>

## Repo evidence inspected

- `<path>` — <why inspected>

## Residual risks

<Unverified feasibility, unavailable evidence, dependency concerns, or "None identified.">
```
