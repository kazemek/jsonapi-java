# Implementation plan review artifact template

Use this template when writing the review artifact. Path and naming rules live in [SKILL.md](SKILL.md).
Severity definitions live in [../review-findings.md](../review-findings.md).

```markdown
# Implementation Plan Review: <plan title>

> **Plan:** `<plan path>`
> **Review scope:** <plan path and inspected module/docs paths>
> **Verdict:** Pass | Changes required | Blocked

## Summary

<Concise conclusion and the most important evidence.>

## Findings

### Blocking
- **<title>** — `<path>:<line or range>`
  - **Planning requirement:** <execution-unit rule, section, acceptance criterion, dependency rule, or design carry-forward>
  - **Impact:** <why this matters>
  - **Recommendation:** <specific correction; require design re-review when architectural>

### Required
- **<title>** — `<path>:<line or range>`
  - **Planning requirement:** <…>
  - **Impact:** <why this matters>
  - **Recommendation:** <specific correction>

### Advisory
- **<title>** — `<path>:<line or range>`
  - **Planning requirement:** <…>
  - **Impact:** <why this matters>
  - **Recommendation:** <specific correction>

<Write "None." under any empty severity group.>

## Contract coverage

| Dimension | Result | Evidence |
|-----------|--------|----------|
| Goal | Pass \| Fail \| Partial \| Not verified | <paths, lines, or explanation> |
| Execution unit | Pass \| Fail \| Partial \| Not verified | <one-context implementation/review and genuine boundaries; size is only a signal> |
| Research and constraints | Pass \| Fail \| Partial \| Not verified | <sources and implementation consequences> |
| Deliverables | Pass \| Fail \| Partial \| Not verified | <evidence> |
| Non-goals | Pass \| Fail \| Partial \| Not verified | <evidence> |
| Implementation boundaries | Pass \| Fail \| Partial \| Not verified | <evidence> |
| Test strategy | Pass \| Fail \| Partial \| Not verified | <evidence> |
| Acceptance criteria | Pass \| Fail \| Partial \| Not verified | <evidence> |
| Dependencies | Pass \| Fail \| Partial \| Not verified | <evidence> |
| Lifecycle and identity | Pass \| Fail \| Partial \| Not verified | <`Not started`/`In progress`; no `Complete` or persistent umbrella; temporary reconciliation window respected; no deletion while references survive> |
| Design Required carry-forward | Pass \| Fail \| Partial \| Not applicable | <design artifacts checked, or why N/A> |
| Nullness / `module-docs` hooks | Pass \| Fail \| Partial \| Not applicable | <why required or not applicable> |

## Repo evidence inspected

- `<path>` — <why inspected>

## Residual risks

<Advisory findings, unverified feasibility, unavailable evidence, dependency concerns, or "None identified.">
```
