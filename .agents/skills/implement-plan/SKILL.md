---
name: implement-plan
description: Implements one implementation plan end-to-end — resolves the contract, reads context, implements, runs completion gates, synchronizes Snapshot/Outlook, and verifies with an implementation review executed by a fresh-context subagent and a bounded fix loop. On Pass, finalizes then deletes the plan. Use when the user asks to implement a plan under `.agentWork/plans/`.
disable-model-invocation: true
---

# Implement Plan

Implement exactly one selected plan and verify it in a context-isolated review. `AGENTS.md` owns
repository authority, lifecycle, canonical synchronization, and completion-gate classification.
Never write `Complete`; after review Pass and finalization, delete the plan.

## Resolve the plan

1. Resolve exactly one user-supplied path or name under `.agentWork/plans/`; ask when several are
   plausible. Never infer the next task from plans, Outlook, source layout, or a reconstructed
   backlog. Linear is optional coordination and never required for a materialized plan.
2. Accept `Not started`. For `In progress`, ask whether to continue the existing implementation or
   start a new attempt. Reject every other status, including `Complete`.
3. Validate `Dependencies` as `None` or hard execution-order prerequisites represented by relative
   Markdown links. If any linked plan still exists, stop Blocked before changing status and do not
   offer an override. If a value is malformed or a linked target is absent, stop because the
   contract is stale/invalid and require `implementation-planning` refinement. Linear and Outlook
   never satisfy a dependency; parallel execution requires refining a `Not started` plan to remove
   the edge, not reinterpreting it here.

## Implement and verify

Read the plan, then follow the task-scoped discovery and authority rules in `AGENTS.md`; do not scan
the repository or deleted plans. Set status to `In progress`, unless already set, then implement
only its deliverables within its non-goals and boundaries. Check acceptance criteria only when
current evidence supports each claim. Use `module-docs` when its trigger applies.

Collect the final path set mechanically from tracked branch/uncommitted changes (`git diff
--name-only` against the base) plus untracked paths (`git ls-files --others --exclude-standard`).
Classify all paths and run only the applicable `AGENTS.md` gates, in its prescribed order. For
module production/test source paths matching `jsonapi-java-*/src/**`, this means
`spotless-format`, `./gradlew clean build`, then `sonar-quality-gate`; `build-logic/src/**` is build
configuration rather than Sonar scope. Do not invent gates beyond the contract and final diff.

An applicable Sonar gate is incomplete without both Quality Gate success and a separate
authenticated Issues API result of zero unresolved new-code issues. CI runs analysis but does not
perform that API check. Without `SONAR_TOKEN`, retain `In progress`: completion requires CI to pass
and the authenticated API check to run separately.

Before review, synchronize affected Snapshot surfaces or record why they remain accurate, and
update, reduce, or delete relevant Outlook when implementation changed future assumptions. No
durable fact may exist only in the temporary plan.

## Review with fresh context

The review is mandatory. Keep the plan through review. Determine the boundary mechanically from
branch and uncommitted Git metadata without summarizing it. Derive `<plan basename>` from the filename without `.md`. Spawn a
new general-purpose, write-capable subagent in a fresh session and send this prompt verbatim,
replacing only `<plan path>` and `<plan basename>`:

```text
You are the implementation reviewer for this repository. Your context was intentionally started
empty so you review independently of the implementing session.

Task inputs (the only facts you may assume):
- Plan: <plan path>
- Review artifact: .agentWork/.session/implementation-review-<plan basename>.md (create or
  completely replace)

Procedure:
1. Read .agents/skills/implementation-review/SKILL.md and follow it exactly.
2. Determine the change set yourself from git metadata (branch and uncommitted changes).
3. Base every conclusion only on the plan contract and repository evidence. Do not accept or
   ask for summaries from the implementing session; ignore editor or IDE state.
4. Write the artifact, then report the artifact path and verdict.
```

Do not provide implementation narrative, summaries, reasoning, self-assessment, commit messages,
or diff content; direct factual questions to repository evidence. If a fresh write-capable subagent
cannot run, follow `.agents/skills/implementation-handoff/SKILL.md` for the plan and
`implementation-review`, print its one-liner, and stop for the fresh-session result.

## Handle the verdict

- `Pass`: finalize only if every applicable gate is verified. An inapplicable gate does not block;
  a blocked/unverified applicable gate retains `In progress` and the plan.
- `Changes required`: fix findings, reclassify the post-fix diff, rerun all applicable gates in
  order, resynchronize Snapshot/Outlook, and review with a new fresh subagent. Allow at most two
  re-reviews; if changes remain after the second, stop with `In progress` and retain the plan.
- `Blocked`: stop with `In progress` and retain the plan.

Replace the fixed review artifact on every re-review; never reuse a reviewer session.

## Finalize after Pass

Only after implementation, gates, canonical sync, fresh review, and review `Pass`, proceed in this
exact order:

1. Find every dependency or meaningful in-body reference to the completing plan under
   `.agentWork/plans/`.
2. Reconcile purely mechanical references directly when completion merely makes them obsolete,
   such as removing a satisfied dependency and writing `None` when no dependencies remain.
3. For semantic changes to a dependent's approach, prerequisites, scope, assumptions, boundaries,
   or intended implementation, do not edit directly. Route a `Not started` dependent through
   `implementation-planning` refinement and its bounded fresh design-review then plan-review
   pipeline while retaining this plan. Never modify an `In progress` dependent; stop finalization
   and retain this plan. Also stop if semantic refinement or review fails.
4. Mechanically verify zero live references by filename, title, or relative link.
5. Update the linked Linear item with concise outcome/coordination status, or explicitly report
   unsynchronized. Linear unavailability is not an engineering gate.
6. Delete the completed plan with `git rm`; leave no `Complete` stub or plans index.

## Report

Report changed paths; verification commands and outcomes; review artifact and verdict;
Snapshot/Outlook sync; mechanical, semantic, or blocked dependent reconciliation; Linear update or
explicit unsync; plan deletion status; and residual risks.
