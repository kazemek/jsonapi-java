# Milestone plan review artifact template

Use this template when writing the review artifact. Path and naming rules live in [SKILL.md](SKILL.md).

```markdown
# Milestone Plan Review: <milestone title>

> **Milestone:** `<milestone path>`
> **Review scope:** <milestone path and inspected index/module/docs paths>
> **Verdict:** Pass | Changes required | Blocked

## Summary

<Concise conclusion and the most important evidence.>

## Findings

### <Severity>: <finding title>

- **Location:** `<path>:<line or range>`
- **Planning requirement:** <size gate, section, acceptance criterion, or index rule>
- **Impact:** <why this matters>
- **Recommendation:** <specific correction>

<Repeat in descending severity. Write "No actionable findings." when none exist.>

## Contract coverage

- [Pass | Fail | Partial | Not verified] Goal
  - Evidence: <paths, lines, or explanation>
- [Pass | Fail | Partial | Not verified] Size gate
  - Evidence: <deliverable/AC counts and outcome coherence>
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
- [Pass | Fail | Partial | Not verified] Index sync
  - Evidence: <dependency order and index entry>
- [Pass | Fail | Partial | Not verified] Lifecycle
  - Evidence: <status and editability>
- [Pass | Fail | Partial | Not applicable] Nullness / `module-docs` hooks
  - Evidence: <why required or why not applicable>

## Repo evidence inspected

- `<path>` — <why inspected>

## Residual risks

<Unverified feasibility, unavailable evidence, dependency concerns, or "None identified.">
```
