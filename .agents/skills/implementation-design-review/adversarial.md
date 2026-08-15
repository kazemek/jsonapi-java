# Adversarial Reviewer

Assume the proposed design is wrong and try to disprove it. Produce a concrete simpler alternative
that still meets the Goal, or state explicitly that no simpler alternative survives the constraints,
with why. Do not implement the planned feature or score execution-unit or size-gate rules, AC
phrasing, or completion-gate lists.

Instruction boundary: do not read [SKILL.md](SKILL.md), [reference.md](reference.md),
[design.md](design.md), or the other reviewer's artifact. You may read
[../review-findings.md](../review-findings.md) for shared severity and stage ownership. Do not
repeat a placement / API / wire-semantics checklist under a different title.

## Resolve inputs

Use the plan and review-artifact paths from the task inputs. Read:

- the target plan;
- accepted ADRs, module READMEs, package/API documentation, architecture rules, public types, and
  specifications linked by the plan or that already constrain this kind of work;
- other live plans under `.agentWork/plans/` only when the live plan explicitly references one;
- additional records only when directly implicated by a candidate simpler alternative.

Inspect current source and tests only to check an actual observable or architectural constraint,
not an incidental implementation pattern. State the reviewed contract boundary in the artifact.
Never imply that unexamined files were reviewed. Do not treat external work-tracker metadata,
deleted plans, or Git archaeology as current engineering truth.

## Perform the review

1. State a concrete simpler alternative that still meets the Goal, **or** explain why none survives
   the constraints. This section is required even on Pass.
2. Look first for:
   - an existing accepted ADR, module/package/API contract, architecture rule, public type, or
     specification that already specifies this kind of work, while the plan forks it;
   - two approaches still present in the plan text without a choice;
   - a new skill, type, or module that an existing skill, type, or module already covers.
3. Follow [../review-findings.md](../review-findings.md): exhaustive pass; classify each finding
   `Blocking`, `Required`, or `Advisory`. Citation-gated rules below constrain when `Blocking` is
   allowed.
4. Give every finding: title, location, **Severity**, **Citation**, impact, recommendation.

## Citation-gated Blocking

`Blocking` only with a repository citation and one of:

- a current canonical constraint named as the established way to do this — accepted ADR,
  module/package/API contract, architecture rule, public type, or specification — **and** the
  simpler alternative still meets the Goal; or
- current source or tests that encode an actual observable or architectural constraint, **and**
  the simpler alternative still meets the Goal; or
- the plan text still presents two approaches without choosing (cite the competing passages).

A live or deleted plan alone must never justify `Blocking`. It may inform a finding, but blocking
requires corroboration from a current canonical constraint above. An incidental implementation
pattern in current source or tests is not architectural authority and must not block a cleaner
future design.

Uncited “could be simpler,” nicer API, or speculative YAGNI → `Advisory`. Completeness gaps that
do not invalidate the architecture → `Required`. Unresolved competing designs → `Blocking`.

## Choose the verdict

- **Blocked:** a *prerequisite* is missing or ambiguous — lifecycle unclear, named
  dependency/ADR/file does not exist, or the spec source the plan relies on cannot be read.
  Stop. Do not guess. Do not use Blocked for disagreement or vagueness.
- **Changes required:** at least one `Blocking` finding.
- **Pass:** no `Blocking` findings. `Required` findings carry into plan review; non-blocking
  alternatives are residual risks.

## Write the artifact

Create `.agentWork/.session/` if needed, then create or completely replace the exact review-artifact
path supplied in the task inputs. Use this template and report the artifact path and verdict.

```markdown
# Adversarial Review: <plan title>

> **Plan:** `<plan path>`
> **Review scope:** <plan path and inspected ADR/plan/module paths>
> **Verdict:** Pass | Changes required | Blocked

## Summary

<Concise conclusion and the most important evidence.>

## Simpler alternative

<Concrete simpler alternative that still meets the Goal, or an explicit statement that no simpler
alternative survives the constraints, with why.>

## Findings

### Blocking
- **<title>** — `<path>:<line or range>`
  - **Citation:** `<path>` — <established-pattern source or competing plan passages; or "None">
  - **Impact:** <why this matters>
  - **Recommendation:** <specific correction>

### Required
- **<title>** — `<path>:<line or range>`
  - **Citation:** `<path>` — <evidence or "None">
  - **Impact:** <why this matters>
  - **Recommendation:** <specific correction>

### Advisory
- **<title>** — `<path>:<line or range>`
  - **Impact:** <why this matters>
  - **Recommendation:** <specific correction>

<Write "None." under any empty severity group.>

## Adversarial coverage

- [Pass | Fail | Partial | Not verified] Simpler alternative stated
  - Evidence: <the alternative, or why none survives>
- [Pass | Fail | Partial | Not applicable] Established-pattern conflict
  - Evidence: <cited ADR/plan/module README, or why none>
- [Pass | Fail | Partial | Not applicable] Unresolved competing designs in the plan
  - Evidence: <competing passages, or why none>

## Repo evidence inspected

- `<path>` — <why inspected>

## Residual risks

<Advisory alternatives, unverified evidence, or "None identified.">
```
