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

- [Pass | Fail | Partial | Not verified] <goal, deliverable, non-goal, dependency, or acceptance criterion>
  - Evidence: <paths, lines, tests, or explanation>
- [Pass | Fail | Partial | Not applicable] `module-docs` checklist
  - Evidence: <public-surface trigger and relevant documentation paths, or why it did not apply>
- [Pass | Fail | Partial | Not applicable] Snapshot sync
  - Evidence: <updated canonical surfaces, or why no documentation change is required>
- [Pass | Fail | Partial | Not applicable] Outlook sync
  - Evidence: <updated/reduced/deleted Outlook, or why Outlook was not implicated>
- [Pass | Fail | Partial | Not verified] Disposability
  - Evidence: <no durable current/future fact exists only in the plan>
- [Pass | Fail | Partial | Not verified] Canonical ownership
  - Evidence: <each new durable fact has one canonical repository owner>

## Verification

- `<command>` — Pass | Fail | Not run
  - Evidence: <result or reason>

## Residual risks

<Unverified behavior, unavailable evidence, dependency concerns, or "None identified.">
```
