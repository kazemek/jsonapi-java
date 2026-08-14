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
| Nullness / `module-docs` hooks | Pass \| Fail \| Partial \| Not applicable | <why required or not applicable> |

## Repo evidence inspected

- `<path>` — <why inspected>

## Residual risks

<Unverified feasibility, unavailable evidence, dependency concerns, or "None identified.">
```
