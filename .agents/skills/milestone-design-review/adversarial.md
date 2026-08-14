# Adversarial Reviewer

Assume the proposed design is wrong and try to disprove it. Produce a concrete simpler alternative
that still meets the Goal, or state explicitly that no simpler alternative survives the constraints,
with why. Do not implement the planned feature or score execution-unit or size-gate rules, AC phrasing, index format, or
completion-gate lists.

Instruction boundary: do not read [SKILL.md](SKILL.md), [design.md](design.md), or the other
reviewer's artifact. Do not repeat a placement / API / wire-semantics checklist under a different
title.

## Resolve inputs

Use the milestone path from the task inputs. Read:

- the target milestone;
- ADRs, completed sibling milestones, and module READMEs linked by the milestone or that already
  specify how this kind of work is done;
- additional records only when directly implicated by a candidate simpler alternative.

Inspect code only to check whether an established pattern already exists. State the reviewed
contract boundary in the artifact. Never imply that unexamined files were reviewed.

## Perform the review

1. State a concrete simpler alternative that still meets the Goal, **or** explain why none survives
   the constraints. This section is required even on Pass.
2. Look first for:
   - an existing ADR, milestone, or module README that already specifies this kind of work, while
     the milestone forks it;
   - two approaches still present in the milestone text without a choice;
   - a new skill, type, or module that an existing skill, type, or module already covers.
3. Assign each finding a severity (Critical / High / Medium / Low) for humans. Severity must **not**
   determine the verdict.
4. Give every finding: title, location, **Blocks:** yes or no, **Citation**, impact, recommendation.

## Citation-gated blocking

`Blocks: yes` only with a repository citation and one of:

- an existing ADR, milestone, or module README named as the established way to do this, **and** the
  simpler alternative still meets the Goal; or
- the milestone text still presents two approaches without choosing (cite the competing passages).

Uncited “could be simpler,” nicer API, or speculative YAGNI → `Blocks: no` (residual). If nothing
blocks, the verdict is Pass even when a simpler alternative was described.

## Choose the verdict

- **Blocked:** a *prerequisite* is missing or ambiguous — lifecycle unclear, named
  dependency/ADR/file does not exist, or the spec source the milestone relies on cannot be read.
  Stop. Do not guess. Do not use Blocked for disagreement or vagueness.
- **Changes required:** at least one finding has `Blocks: yes`.
- **Pass:** no finding has `Blocks: yes`. Non-blocking alternatives are residual risks.

## Write the artifact

Create `.agentWork/.session/` if needed, then create or completely replace:

```text
.agentWork/.session/milestone-design-review-adversarial-<milestone-basename>.md
```

Use this template. Replace the prior artifact on re-review. Report the artifact path and verdict.

```markdown
# Adversarial Review: <milestone title>

> **Milestone:** `<milestone path>`
> **Review scope:** <milestone path and inspected ADR/milestone/module paths>
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
- **Citation:** `<path>` — <established-pattern source or competing milestone passages; or "None">
- **Impact:** <why this matters>
- **Recommendation:** <specific correction>

<Repeat in descending severity. Write "No findings." when none exist.>

## Adversarial coverage

- [Pass | Fail | Partial | Not verified] Simpler alternative stated
  - Evidence: <the alternative, or why none survives>
- [Pass | Fail | Partial | Not applicable] Established-pattern conflict
  - Evidence: <cited ADR/milestone/module README, or why none>
- [Pass | Fail | Partial | Not applicable] Unresolved competing designs in the milestone
  - Evidence: <competing passages, or why none>

## Repo evidence inspected

- `<path>` — <why inspected>

## Residual risks

<Non-blocking alternatives, unverified evidence, or "None identified.">
```
