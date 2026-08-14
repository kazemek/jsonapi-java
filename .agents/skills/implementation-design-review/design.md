# Design Reviewer

Review whether the plan's technical design is sound: placement, public API, and JSON:API wire
semantics as this repository specifies them. Do not implement the planned feature, invent a simpler
alternative, or score execution-unit or size-gate rules, AC phrasing, or completion-gate lists.

Instruction boundary: do not read [SKILL.md](SKILL.md), [reference.md](reference.md),
[adversarial.md](adversarial.md), or the other reviewer's artifact.

## Resolve inputs

Use the plan and review-artifact paths from the task inputs. Read:

- the target plan;
- only directly implicated `AGENTS.md` sections: relevant architecture or completion constraints
  for product/API plans; and routing, knowledge/lifecycle, review, or other governing workflow
  sections for workflow-agent plans. Use those sections to locate linked documentation as needed;
- `docs/vision.md` when the plan changes project direction, modules, or public product
  boundaries, or when the spec suggests a vision conflict;
- relevant Outlook when the plan concerns unbuilt or revisable future direction (planning
  context and future pressure only; never current truth or a dependency);
- ADRs and conformance sections linked by the plan or affected module documentation, plus
  additional records only when directly implicated;
- affected module READMEs, `package-info.java`, and narrow production types when the plan
  names them.

Inspect code only to check design claims — not to score an implementation. State the reviewed
contract boundary in the artifact. Never imply that unexamined files were reviewed. Do not treat
external work-tracker metadata, deleted plans, or Git archaeology as current engineering truth.

## Workflow / docs N/A

If the plan does not change product types, modules, or wire semantics, mark product-design
coverage dimensions Not applicable. Evaluate workflow placement and workflow semantics, including
orchestration, handoff, artifact, verdict, and lifecycle contracts. Do not invent JSON:API findings.

## Perform the review

1. Map Goal, Research and constraints, Deliverables, Non-goals, and Implementation boundaries to
   the proposed technical approach.
2. Look first for:
   - vision or ADR conflict (product boundary, core with no runtime deps, application policy
     explicit);
   - wrong module or package placement, dependency direction, or ArchUnit/allowlist implications;
   - public types that can silently represent an illegal state; missing sealed invalid-state
     prevention; JSpecify absent vs wire-null collapse;
   - JSON:API wire-semantic collapse (omitted member vs JSON `null`, linkage vs `included`,
     member-name grammar);
   - hidden application policy in traversal, mapping, or adapter defaults;
   - implicit design: the approach is not stated in Goal, Research and constraints, or
     Implementation boundaries.
3. Do not recommend a different principal mechanism as a "simpler alternative." Record placement or
   contract conflicts only.
4. Assign each finding a severity (Critical / High / Medium / Low) for humans. Severity must **not**
   determine the verdict.
5. Give every finding: title, location, **Blocks:** yes or no, **Citation**, impact, recommendation.

## Citation-gated blocking

`Blocks: yes` only with a repository citation **and** a shown conflict:

- a vision section, accepted ADR, JSON:API spec rule, existing module/allowlist, or existing public
  type;
- conflict kinds: wrong placement, wire-semantic collapse, illegal state representable, hidden
  application policy.

Outlook must not justify `Blocks: yes`. Intentional divergence from Outlook that still satisfies
Snapshot, Vision, accepted ADRs, specifications, and current repository evidence is not a
design-review failure.

Uncited taste or hypothetical nicer APIs → `Blocks: no` (residual). Implicit design (approach not
stated) may `Blocks: yes` citing the empty or non-deciding Goal / Research / Boundaries passages.

## Choose the verdict

- **Blocked:** a *prerequisite* is missing or ambiguous — lifecycle unclear, named
  dependency/ADR/file does not exist, or the spec source the plan relies on cannot be read.
  Stop. Do not guess. Do not use Blocked for disagreement or vagueness.
- **Changes required:** at least one finding has `Blocks: yes`. Includes implicit design (write it
  down).
- **Pass:** no finding has `Blocks: yes`. Non-blocking findings are residual risks.

## Write the artifact

Create `.agentWork/.session/` if needed, then create or completely replace the exact review-artifact
path supplied in the task inputs. Use this template and report the artifact path and verdict.

```markdown
# Design Review: <plan title>

> **Plan:** `<plan path>`
> **Review scope:** <plan path and inspected vision/ADR/module/docs paths>
> **Verdict:** Pass | Changes required | Blocked

## Summary

<Concise conclusion and the most important evidence.>

## Findings

### <Severity>: <finding title>

- **Location:** `<path>:<line or range>`
- **Blocks:** yes | no
- **Citation:** `<path>` — <vision section, ADR, spec rule, module/allowlist, or public type; or "None">
- **Impact:** <why this matters>
- **Recommendation:** <specific correction>

<Repeat in descending severity. Write "No findings." when none exist.>

## Design coverage

- [Pass | Fail | Partial | Not applicable] Placement / module boundaries
  - Evidence: <paths, lines, or why N/A>
- [Pass | Fail | Partial | Not applicable] Vision / ADR alignment
  - Evidence: <…>
- [Pass | Fail | Partial | Not applicable] Public API / invalid states
  - Evidence: <…>
- [Pass | Fail | Partial | Not applicable] Wire semantics
  - Evidence: <…>
- [Pass | Fail | Partial | Not applicable] Hidden application policy
  - Evidence: <…>
- [Pass | Fail | Partial | Not applicable] Workflow placement / semantics
  - Evidence: <placement and orchestration/handoff/artifact/verdict/lifecycle, or why N/A>

## Repo evidence inspected

- `<path>` — <why inspected>

## Residual risks

<Non-blocking findings, unverified evidence, or "None identified.">
```
