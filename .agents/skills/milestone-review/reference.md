# Milestone review artifact template

Use this template when writing the review artifact. Path and naming rules live in [SKILL.md](SKILL.md).

```markdown
# Milestone Review: <milestone title>

> **Milestone:** `<milestone path>`
> **Review scope:** <diff, commit range, or inspected paths>
> **Verdict:** Pass | Changes required | Blocked

## Summary

<Concise conclusion and the most important evidence.>

## Findings

### <Severity>: <finding title>

- **Location:** `<path>:<line or range>`
- **Milestone requirement:** <deliverable or acceptance criterion>
- **Impact:** <why this matters>
- **Recommendation:** <specific correction>

<Repeat in descending severity. Write "No actionable findings." when none exist.>

## Milestone coverage

- [Pass | Fail | Partial | Not verified] <goal, deliverable, non-goal, dependency, or acceptance criterion>
  - Evidence: <paths, lines, tests, or explanation>
- [Pass | Fail | Partial | Not applicable] `module-docs` checklist
  - Evidence: <public-surface trigger and relevant documentation paths, or why it did not apply>

## Verification

- `<command>` — Pass | Fail | Not run
  - Evidence: <result or reason>

## Residual risks

<Unverified behavior, unavailable evidence, dependency concerns, or "None identified.">
```
