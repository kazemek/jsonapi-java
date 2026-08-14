# Implementation review artifact template

Use this template when writing the review artifact. Path and naming rules live in [SKILL.md](SKILL.md).

```markdown
# Implementation Review: <plan title>

> **Plan:** `<plan path>`
> **Review scope:** <diff, commit range, or inspected paths>
> **Verdict:** Pass | Changes required | Blocked

## Summary

<Concise conclusion and the most important evidence.>

## Findings

### <Severity>: <finding title>

- **Location:** `<path>:<line or range>`
- **Plan requirement:** <deliverable or acceptance criterion>
- **Impact:** <why this matters>
- **Recommendation:** <specific correction>

<Repeat in descending severity. Write "No actionable findings." when none exist.>

## Plan coverage

| Dimension | Result | Evidence |
|-----------|--------|----------|
| <goal, deliverable, non-goal, dependency, or acceptance criterion> | Pass \| Fail \| Partial \| Not verified | <paths, lines, tests, or explanation> |
| Nullness | Pass \| Fail \| Partial \| Not applicable | <contract and annotation evidence> |
| `module-docs` | Pass \| Fail \| Partial \| Not applicable | <trigger and documentation paths, or why not applicable> |
| Snapshot sync | Pass \| Fail \| Partial \| Not applicable | <updated canonical surfaces, or why no change is required> |
| Outlook sync | Pass \| Fail \| Partial \| Not applicable | <updated/reduced/deleted Outlook, or why not implicated> |
| Disposability | Pass \| Fail \| Partial \| Not verified | <no durable fact exists only in the plan> |
| Canonical ownership | Pass \| Fail \| Partial \| Not verified | <one canonical owner for each new durable fact> |

<Repeat the first row for the goal, every deliverable, non-goal, dependency, and acceptance criterion.>

## Verification

- `<command>` — Pass | Fail | Not run
  - Evidence: <result or reason>

## Residual risks

<Unverified behavior, unavailable evidence, dependency concerns, or "None identified.">
```
