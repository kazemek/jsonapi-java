---
name: implement-work
description: Implements a requested work outcome end-to-end — reads current repository evidence, implements, runs applicable completion gates, and verifies with a fresh-context Implementation Review and a bounded fix loop. A gitignored local working plan may be used as optional context but is not required. Use when the user asks to implement requested work or to build after planning.
disable-model-invocation: true
---

# Implement Work

Implement the **requested outcome / acceptance intent**. A local working plan under
`.agentWork/plans/` is optional context only—not a gate, not correctness authority, and not
required. `AGENTS.md` owns completion-gate classification.

## Resolve the work

1. Resolve the explicit requested outcome and acceptance intent from the user (or fallback
   `.agentWork/.session/work-context.md` when the harness cannot pass intent otherwise). Ask when
   ambiguous. A direct user request is sufficient; no external tracker is required.
2. Optionally read a local working plan for orientation. Do not treat plan divergence as failure by
   itself.
3. Never infer the next task from plans directory listing, Git history, or a reconstructed backlog.

## Implement

Follow task-scoped discovery in `AGENTS.md`. Implement against:

- requested outcome / acceptance intent;
- current repository state;
- Vision / ADRs / canonical docs where applicable;
- public/API contracts;
- tests and applicable completion gates.

Local implementation freedom is expected for harmless details (helpers, naming, extraction,
ordinary control flow) when material architecture, behavior, compatibility, ownership,
dependencies, constraints, verification, and acceptance intent are preserved.

If repository reality contradicts a **material** product/design/scope decision, stop and ask the
maintainer (prefer the harness interactive question facility). Do not silently redesign.

Use `module-docs` when its trigger applies.

## Completion gates

Resolve the change-set from branch and uncommitted Git metadata against an explicit or remote HEAD
base. Classify paths and run only applicable `AGENTS.md` gates in order. Local source completion does
not include a SonarCloud round-trip; CI remains the authority for Quality Gate and zero
unresolved new-code issues.

Synchronize affected Snapshot surfaces (module README, package docs, ADR/conformance as needed) so
no durable fact exists only in a disposable local plan.

## Review with fresh context

Implementation Review is mandatory. Prefer embedding the requested outcome and acceptance intent
**directly in the reviewer prompt**. Write `.agentWork/.session/work-context.md` only if the
harness cannot reliably pass that intent to a fresh reviewer.

Derive a basename slug from the work title or local plan filename. Spawn a new general-purpose,
write-capable subagent in a fresh session:

```text
You are the implementation reviewer for this repository. Your context was intentionally started
empty so you review independently of the implementing session.

Task inputs (the only facts you may assume):
- Requested outcome: <outcome>
- Acceptance intent: <acceptance intent>
- Review artifact: .agentWork/.session/implementation-review-<basename>.md (create or completely
  replace)
- Optional local plan (context only, not authority): <path or none>
- Optional work-context fallback path: <path or none>

Procedure:
1. Read .agents/skills/implementation-review/SKILL.md and follow it exactly.
2. Determine the change set yourself from git metadata (branch and uncommitted changes).
3. Evaluate requested outcome / acceptance intent + actual diff + repository contracts/docs/tests +
   applicable gates. Do not reconstruct the requested work from the local plan, branch names,
   trackers, Git history, or inferred code alone.
4. Write the artifact, then report the artifact path and verdict.
```

If a fresh write-capable subagent cannot run, follow
[../implementation-handoff/SKILL.md](../implementation-handoff/SKILL.md).

## Handle the verdict

- **Pass:** done when applicable gates are verified.
- **Changes required:** fix findings, reclassify the diff, rerun applicable gates, and review with a
  new fresh subagent. At most two re-reviews; if changes remain, stop for the maintainer.
- **Blocked:** stop and retain incomplete work for the maintainer.

## After Pass

Delete the local working plan when it is no longer useful (it is gitignored; no merge-readiness
gate depends on plan deletion). Report changed paths, gate outcomes, review artifact/verdict, and
residual risks.
