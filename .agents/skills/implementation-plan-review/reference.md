# Implementation plan review reference

Severity definitions: [../review-findings.md](../review-findings.md). Fixed artifact paths also
listed in [../implementation-design-review/reference.md](../implementation-design-review/reference.md).

## Placeholders

Derive `<basename>` from the local plan filename without `.md`, or from a short slug for plan-less
approach text. If approach text is supplied without a durable path, materialize it once to:

```text
.agentWork/.session/plan-source-<basename>.md
```

and pass that path as the approach source. Do not create a local `.agentWork/plans/` file solely to
request Plan Review.

## Spawn prompt

Replace only `<outcome>`, `<acceptance intent>`, `<approach source path>`, and `<basename>`.

```text
You are the implementation plan reviewer for this repository. Your context was intentionally
started empty so you review independently of the planning session.

Task inputs (the only facts you may assume):
- Requested outcome: <outcome>
- Acceptance intent: <acceptance intent>
- Plan or approach source: <approach source path>
- Review artifact: .agentWork/.session/implementation-plan-review-<basename>.md (create or
  completely replace)

Procedure:
1. Read .agents/skills/implementation-plan-review/SKILL.md and follow the Reviewer procedure
   exactly.
2. Base every conclusion only on the supplied outcome, acceptance intent, plan/approach source,
   and repository evidence. Do not accept or ask for summaries from the planning session; ignore
   editor or IDE state.
3. Write the artifact, then report the artifact path and assessment. Do not mutate the plan or
   approach source.
```

## Artifact template

```markdown
# Implementation Plan Review: <title>

> **Requested outcome:** <outcome>
> **Acceptance intent:** <acceptance intent>
> **Plan / approach source:** `<path>`
> **Review scope:** <inspected paths>
> **Assessment:** No material concerns | Concerns found | Unable to assess

## Summary

## Findings

### Blocking
- **<title>** — `<path>:<line>`
  - **Severity:** Blocking
  - **Citation:** `<path>` — <evidence or "None">
  - **Impact:**
  - **Recommendation:**

### Required
- **<title>** — `<path>:<line>`
  - **Severity:** Required
  - **Citation:** `<path>` — <evidence or "None">
  - **Impact:**
  - **Recommendation:**

### Advisory
- **<title>** — `<path>:<line>`
  - **Severity:** Advisory
  - **Citation:** `<path>` — <evidence or "None">
  - **Impact:**
  - **Recommendation:**

<Write "None." under any empty severity group.>

## Contract coverage

| Dimension | Result | Evidence |
|-----------|--------|----------|
| Goal | Pass \| Fail \| Partial \| Not verified | |
| Approach coherence | Pass \| Fail \| Partial \| Not verified | |
| Constraints / ownership | Pass \| Fail \| Partial \| Not verified | |
| Checks / verification | Pass \| Fail \| Partial \| Not verified | |
| Over-specification | Pass \| Fail \| Partial \| Not verified | |

## Repo evidence inspected

## Residual risks
```
