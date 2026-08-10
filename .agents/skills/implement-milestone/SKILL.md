---
name: implement-milestone
description: Implements one milestone end-to-end — resolves the contract, reads context, implements, runs completion gates, and verifies with a milestone review executed by a fresh-context subagent and a bounded fix loop. Use when the user asks to implement a milestone under `.agentWork/milestones/`.
disable-model-invocation: true
---

# Implement Milestone

Implement exactly one milestone contract and verify it with a context-isolated review. The review
must never see this session's context or reasoning; it is executed by a fresh subagent that derives
everything from the milestone contract and repository evidence.

## Resolve the milestone

1. Identify exactly one target file under `.agentWork/milestones/` from a path, phase, or milestone
   name supplied by the user. Ask the user to choose when several milestones are plausible.
2. Check the milestone `Status`:
   - `Not started` — proceed.
   - `Complete` — do not re-implement; report that the milestone is already delivered and stop.
   - `In progress` — ask whether to continue the existing implementation or start a new attempt.
3. Check dependencies: when the milestone lists dependencies that are not `Complete`, warn the user
   and ask before proceeding; the milestone index explicitly allows some tracks to proceed in
   parallel.

## Read context

Follow the task-scoped discovery route for implementation in `AGENTS.md`:

- the target milestone;
- `AGENTS.md` and `.agentWork/milestones/README.md`;
- affected module READMEs and package documentation;
- `docs/vision.md` only when the milestone changes project direction, modules, or public product
  boundaries;
- ADRs and conformance sections linked by the milestone or affected module documentation.

Do not scan the whole repository.

## Implement

1. Set the milestone `Status` to `In progress` in the milestone file and in the matching index
   entry in `.agentWork/milestones/README.md` unless it already is.
2. Deliver the milestone's deliverables within its non-goals and implementation boundaries. Do not
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
   Never require a gate the milestone's acceptance criteria or the change scope does not demand.

## Review with fresh context

The review is mandatory and non-negotiable. Its purpose is to verify the implementation without any
influence from this session's context or reasoning.

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
   direct it to repository evidence (files, git history, the milestone contract).
5. When the harness cannot spawn a write-capable fresh subagent, fall back to a manual fresh
   session: follow `.agents/skills/milestone-handoff/SKILL.md` with the milestone path and suggested
   skill `milestone-review`, then print the one-liner it produces.

### Reviewer prompt (send verbatim)

```text
You are the milestone reviewer for this repository. Your context was intentionally started empty
so you review independently of the implementing session.

Task inputs (the only facts you may assume):
- Milestone: <milestone path>
- Review artifact: .agentWork/.session/milestone-review-<milestone basename>.md (create or
  completely replace)

Procedure:
1. Read .agents/skills/milestone-review/SKILL.md and follow it exactly.
2. Determine the change set yourself from git metadata (branch and uncommitted changes).
3. Base every conclusion only on the milestone contract and repository evidence. Do not accept or
   ask for summaries from the implementing session; ignore editor or IDE state.
4. Write the artifact, then report the artifact path and verdict.
```

## Handle the verdict

- **Pass:** when all applicable completion gates pass, set the milestone `Status` to `Complete` in
  the milestone file and the index entry. A gate that is inapplicable to the change scope (for
  example Sonar for docs-only or workflow-only work) does not block completion. If an applicable
  gate is blocked or unverified — for example Sonar without `SONAR_TOKEN` — keep `Status`
  `In progress` and report the remaining gate as the completion blocker.
- **Changes required:** fix the findings, re-run all applicable completion gates on the post-fix
  state (per the change-scope tiers: `clean build`, Spotless, Sonar), then re-run the review with a
  NEW fresh subagent. Cap the loop
  at two re-reviews; when the verdict is still `Changes required`, stop and report the remaining
  findings. Keep `Status` `In progress` in the milestone file and index entry until a review
  passes on the post-fix state.
- **Blocked:** stop and report; keep `Status` `In progress` in the milestone file and index entry.

## Report

Report to the user: the changed files, verification commands run and their outcomes, the review
artifact path, the verdict, and residual risks.
