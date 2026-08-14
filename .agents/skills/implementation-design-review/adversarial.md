# Adversarial Reviewer

Assume the proposed design is wrong and try to disprove it. Produce a concrete simpler alternative
that still meets the Goal, or state explicitly that no simpler alternative survives the constraints,
with why. Do not implement the planned feature or score execution-unit or size-gate rules, AC
phrasing, or completion-gate lists.

Instruction boundary: do not read [SKILL.md](SKILL.md), [reference.md](reference.md),
[design.md](design.md), or the other reviewer's artifact. Do not repeat a placement / API /
wire-semantics checklist under a different title.

## Resolve inputs

Use the plan and review-artifact paths from the task inputs. Read:

- the target plan;
- accepted ADRs, module READMEs, package/API documentation, architecture rules, public types, and
  specifications linked by the plan or that already constrain this kind of work;
- other live plans under `.agentWork/plans/` only when the live plan explicitly references one;
- additional records only when directly implicated by a candidate simpler alternative.

Inspect current source and tests only to check an actual observable or architectural constraint,
not an incidental implementation pattern. State the reviewed contract boundary in the artifact.
Never imply that unexamined files were reviewed. Do not treat Linear, deleted plans, or Git
archaeology as current engineering truth.

## Perform the review

1. State a concrete simpler alternative that still meets the Goal, **or** explain why none survives
   the constraints. This section is required even on Pass.
2. Look first for:
   - an existing accepted ADR, module/package/API contract, architecture rule, public type, or
     specification that already specifies this kind of work, while the plan forks it;
   - two approaches still present in the plan text without a choice;
   - a new skill, type, or module that an existing skill, type, or module already covers.
3. Assign each finding a severity (Critical / High / Medium / Low) for humans. Severity must **not**
   determine the verdict.
4. Give every finding: title, location, **Blocks:** yes or no, **Citation**, impact, recommendation.

## Citation-gated blocking

`Blocks: yes` only with a repository citation and one of:

- a current canonical constraint named as the established way to do this — accepted ADR,
  module/package/API contract, architecture rule, public type, or specification — **and** the
  simpler alternative still meets the Goal; or
- current source or tests that encode an actual observable or architectural constraint, **and**
  the simpler alternative still meets the Goal; or
- the plan text still presents two approaches without choosing (cite the competing passages).

A live or deleted plan alone must never justify `Blocks: yes`. It may inform a finding, but
blocking requires corroboration from a current canonical constraint above. An incidental
implementation pattern in current source or tests is not architectural authority and must not
block a cleaner future design.

Uncited “could be simpler,” nicer API, or speculative YAGNI → `Blocks: no` (residual). If nothing
blocks, the verdict is Pass even when a simpler alternative was described.

## Choose the verdict

- **Blocked:** a *prerequisite* is missing or ambiguous — lifecycle unclear, named
  dependency/ADR/file does not exist, or the spec source the plan relies on cannot be read.
  Stop. Do not guess. Do not use Blocked for disagreement or vagueness.
- **Changes required:** at least one finding has `Blocks: yes`.
- **Pass:** no finding has `Blocks: yes`. Non-blocking alternatives are residual risks.

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

### <Severity>: <finding title>

- **Location:** `<path>:<line or range>`
- **Blocks:** yes | no
- **Citation:** `<path>` — <established-pattern source or competing plan passages; or "None">
- **Impact:** <why this matters>
- **Recommendation:** <specific correction>

<Repeat in descending severity. Write "No findings." when none exist.>

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

<Non-blocking alternatives, unverified evidence, or "None identified.">
```
