---
name: implement-plan
description: Implements one implementation plan end-to-end — resolves the contract, reads context, implements, runs completion gates, synchronizes Snapshot/Outlook, and verifies with an implementation review executed by a fresh-context subagent and a bounded fix loop. On Pass, finalizes then deletes the plan. Use when the user asks to implement a plan under `.agentWork/plans/`.
disable-model-invocation: true
---

# Implement Plan

Implement exactly one plan contract and verify it with a context-isolated review. The review
must never see this session's context or reasoning; it is executed by a fresh subagent that derives
everything from the plan contract and repository evidence.

On review Pass, do not write `Complete`. Run post-review finalization, then delete the plan file.

## Resolve the plan

1. Identify exactly one target file under `.agentWork/plans/` from a path or plan name supplied by
   the user. Ask the user to choose when several plans are plausible. Do not infer the next task
   from `.agentWork/plans/`, Outlook, source layout, or a reconstructed backlog.
2. Check the plan `Status`:
   - `Not started` — proceed.
   - `In progress` — ask whether to continue the existing implementation or start a new attempt.
   - Any other value, including `Complete`, is invalid. Stop and report; do not implement.
3. Check dependencies: listed `Dependencies` are hard execution-order prerequisites — relative
   Markdown links to other live plan files, or `None`.
   - `Dependencies: None` — proceed.
   - For every relative Markdown-linked dependency whose target file still exists under
     `.agentWork/plans/`: implementation is **BLOCKED**. Report the unresolved dependency. Do not
     set the target plan to `In progress`. Do not offer to override the dependency interactively.
   - If a listed dependency path does not exist: the plan contract is stale/invalid. Stop, report
     the stale reference, and require the plan to be refined through `implementation-planning`.
   - Outlook and Linear never satisfy listed Dependencies. If two plans should run in parallel,
     refine the still-`Not started` plan through `implementation-planning` and remove the edge;
     `implement-plan` must not reinterpret or override a reviewed execution contract. Linear is
     never required to implement or review this already-materialized plan.

Without Linear, implement only an explicitly selected or already-materialized repository plan.
Never infer the next project task from `.agentWork/plans/`.

## Read context

Follow the task-scoped discovery route for implementation in `AGENTS.md`:

- the target plan;
- `AGENTS.md`;
- affected module READMEs and package documentation;
- `docs/vision.md` only when the plan changes project direction, modules, or public product
  boundaries;
- `docs/outlook/` only when the work is about unbuilt or revisable future direction; Outlook is
  never current truth and never satisfies dependencies;
- ADRs and conformance sections linked by the plan or affected module documentation.

Do not scan the whole repository. Do not search historical or deleted plans for current
engineering truth.

## Implement

1. Set the plan `Status` to `In progress` in the plan file unless it already is. Never write
   `Complete`.
2. Deliver the plan's deliverables within its non-goals and implementation boundaries. Do not
   add work outside the contract.
3. Mark acceptance criteria `[x]` only when implementation evidence exists at that point. These are
   claims the review verifies; never leave checkbox state to the reviewer, and never let the
   reviewer edit it.
4. If public module surface changed (packages, entry points, validate/read flows, non-goals, or
   agent-relevant invariants), follow the `module-docs` skill; reference it rather than duplicating
   its checklist.
5. Classify the change scope from the diff and run only the applicable completion gates per the
   change-scope gate tiers in `AGENTS.md`. Collect the path set mechanically: tracked changes via
   `git diff --name-only` against the base plus untracked files via `git ls-files --others
   --exclude-standard` (untracked files would otherwise be absent from classification until staged
   or committed):
   - docs-only and workflow-only changes need no build, Spotless, or Sonar;
   - build-configuration changes need `./gradlew clean build` (plus Spotless when Spotless-covered
     files or the formatter configuration changed);
   - production/test source changes need the full gates:
     - `spotless-format` skill: `./gradlew spotlessApply` then `./gradlew spotlessCheck` — run it
       before the build so the build's own `spotlessCheck` passes on the first run;
     - `./gradlew clean build` passes;
     - `sonar-quality-gate` skill: without `SONAR_TOKEN`, Sonar is blocked and must not count as a
       completed gate for source-scope work; keep `Status` `In progress` until CI confirms the
       Quality Gate and zero new-code issues via the Issues API.
   Never require a gate the plan's acceptance criteria or the change scope does not demand.
6. Synchronize canonical documentation before review:
   - update affected Snapshot surfaces, or record that no documentation change is required because
     current knowledge remains accurate;
   - update, reduce, or delete relevant Outlook when the implementation changed future assumptions.
   The plan must not be the only place a newly durable current or future fact exists.

## Review with fresh context

The review is mandatory and non-negotiable. Its purpose is to verify the implementation without any
influence from this session's context or reasoning. Keep the plan file on disk through the review
so the reviewer can compare contract vs delivery.

1. Determine the change-set boundary mechanically from git metadata (branch changes and uncommitted
   changes). Do not summarize or interpret it; the reviewer derives it itself.
2. Spawn a NEW general-purpose subagent with write access (for example, opencode `general` or the
   equivalent general subagent in the harness in use):
   - fresh context: never resume or reuse a previous subagent session;
   - write capability: it must create the review artifact under `.agentWork/.session/`.
3. Send the reviewer prompt below verbatim, filling only the placeholder. Do not add anything
   to it: no summaries, self-assessment, reasoning, implementation narrative, commit messages, or
   diff content.
4. Never answer the reviewer's questions with implementation narrative. When it asks for facts,
   direct it to repository evidence (files, git history, the plan contract).
5. When the harness cannot spawn a write-capable fresh subagent, fall back to a manual fresh
   session: follow `.agents/skills/implementation-handoff/SKILL.md` with the plan path and suggested
   skill `implementation-review`, then print the one-liner it produces.

### Reviewer prompt (send verbatim)

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

## Handle the verdict

- **Pass:** when all applicable completion gates pass, run **Finalization after Pass** below. A
  gate that is inapplicable to the change scope (for example Sonar for docs-only or workflow-only
  work) does not block finalization. If an applicable gate is blocked or unverified — for example
  Sonar without `SONAR_TOKEN` — keep `Status` `In progress`, do not delete the plan, and report the
  remaining gate as the completion blocker.
- **Changes required:** fix the findings, re-run all applicable completion gates on the post-fix
  state (per the change-scope tiers: `clean build`, Spotless, Sonar), re-synchronize Snapshot and
  relevant Outlook as needed, then re-run the review with a NEW fresh subagent. Cap the loop
  at two re-reviews; when the verdict is still `Changes required`, stop and report the remaining
  findings. Keep `Status` `In progress` until a review passes on the post-fix state. Do not delete
  the plan.
- **Blocked:** stop and report; keep `Status` `In progress`. Do not delete the plan.

## Finalization after Pass

Do this only after a review Pass. Do not write `Complete`. Order:

```text
implementation + gates
→ Snapshot / Outlook sync
→ fresh implementation review
→ review Pass
→ reconcile dependent live plans
→ mechanically verify no remaining live refs
→ update Linear OR report unsync
→ delete completed plan
```

The first four steps are already done when this section runs. Then:

1. Reconcile dependent live plans under `.agentWork/plans/` so no live `Dependencies` header or
   in-body reference points at this plan. Retarget or remove those references; durable facts belong
   in Snapshot or Outlook, not in a soon-to-be-deleted contract.
2. Mechanically verify no remaining live references to this plan (filename, title, or relative
   link) under `.agentWork/plans/`.
3. Update the linked Linear work item with a concise outcome and coordination status, **or**
   explicitly report that Linear synchronization is unavailable. Linear unavailability must not
   fail a correct implementation or preserve engineering knowledge in the plan, but must never be
   silently ignored. No engineering correctness gate requires Linear.
4. Delete the plan file (`git rm` the path under `.agentWork/plans/`). Do not leave a `Complete`
   stub. Do not create `.agentWork/plans/README.md`.

## Report

Report to the user: the changed files, verification commands run and their outcomes, the review
artifact path, the verdict, Snapshot/Outlook sync result, dependent-plan reconciliation, Linear
update or explicit unsync, whether the plan file was deleted, and residual risks.
