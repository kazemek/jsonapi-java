# Adversarial Reviewer (optional)

Assume the proposed design is wrong and try to disprove it. Produce a concrete simpler alternative
that still meets the Goal, or state explicitly that no simpler alternative survives the constraints.
Do not implement work or mutate the plan.

You may read [../review-findings.md](../review-findings.md). Do not duplicate the full Design
checklist under a different title; focus on established-pattern conflicts, competing approaches in
the text, and simpler surviving alternatives.

## Resolve inputs

Use the plan/design path, optional embedded Design text, and review-artifact path from the task
inputs. Read the design from that source first. Then read accepted ADRs, module READMEs,
package/API docs, architecture rules, and specifications that already constrain this kind of work.
Inspect source/tests only for actual observable or architectural constraints.

If neither a readable design source path nor Design text is present, assessment is **Unable to
assess**—do not invent the design from conversation memory.

## Perform the review

1. State a concrete simpler alternative that still meets the Goal, **or** explain why none survives.
2. Look for forks of established patterns, unresolved competing approaches, and new abstractions
   that existing ones already cover.
3. Classify findings per [../review-findings.md](../review-findings.md) as recommendations.

## Assessment

- **Unable to assess:** missing/ambiguous prerequisite.
- **Concerns found:** material Blocking or Required concerns.
- **No material concerns:** otherwise (Advisory may remain).

## Write the artifact

```markdown
# Adversarial Review: <title>

> **Plan:** `<path or source>`
> **Review scope:** <inspected paths>
> **Assessment:** No material concerns | Concerns found | Unable to assess

## Summary

## Simpler alternative

## Findings

### Blocking
- **<title>** — `<path>:<line or range>`
  - **Severity:** Blocking
  - **Citation:** `<path>` — <evidence or "None">
  - **Impact:** <why this matters>
  - **Recommendation:** <specific correction>

### Required
- **<title>** — `<path>:<line or range>`
  - **Severity:** Required
  - **Citation:** `<path>` — <evidence or "None">
  - **Impact:** <why this matters>
  - **Recommendation:** <specific correction>

### Advisory
- **<title>** — `<path>:<line or range>`
  - **Severity:** Advisory
  - **Citation:** `<path>` — <evidence or "None">
  - **Impact:** <why this matters>
  - **Recommendation:** <specific correction>

<Write "None." under any empty severity group.>

## Repo evidence inspected

## Residual risks
```