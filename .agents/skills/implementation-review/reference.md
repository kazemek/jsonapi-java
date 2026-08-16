# Implementation review artifact template

```markdown
# Implementation Review: <title>

> **Requested outcome:** <outcome>
> **Acceptance intent:** <acceptance intent>
> **Review scope:** <diff, commit range, or inspected paths>
> **Verdict:** Pass | Changes required | Blocked

## Summary

## Findings

### <Severity>: <finding title>

- **Location:** `<path>:<line or range>`
- **Requirement:** <outcome / acceptance intent element>
- **Impact:** <why this matters>
- **Recommendation:** <specific correction>

## Outcome coverage

| Dimension | Result | Evidence |
|-----------|--------|----------|
| <outcome / acceptance element> | Pass \| Fail \| Partial \| Not verified | |
| Nullness | Pass \| Fail \| Partial \| Not applicable | |
| `module-docs` | Pass \| Fail \| Partial \| Not applicable | |
| Snapshot sync | Pass \| Fail \| Partial \| Not applicable | |
| Canonical ownership | Pass \| Fail \| Partial \| Not verified | |

## Verification

- `<command>` — Pass | Fail | Not run

## Residual risks
```
