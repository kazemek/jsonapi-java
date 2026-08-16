---
name: implementation-planning
description: Researches current repository reality and maintains an optional gitignored local working plan for one coherent implementation increment. Supports interactive maintainer planning and optional Design/Plan Review. Use when the user explicitly requests implementation planning or approach refinement.
disable-model-invocation: true
---

# Implementation Planning

Create or refine optional **local working memory** under gitignored `.agentWork/plans/`. Plans are
not engineering truth, not backlog, and not permission to implement. `AGENTS.md` owns repository
authority and completion gates.

Shared finding semantics: [../review-findings.md](../review-findings.md). Template and examples:
[reference.md](reference.md).

## Resolve the requested work

1. Resolve an explicit requested outcome / acceptance intent from the user (or optional
   `.agentWork/.session/work-context.md` when present). A direct user request is sufficient; no
   external tracker is required.
2. Decide whether the work is **one coherent implementation increment** against current repository
   reality (can be implemented and independently reviewed as one change now).
3. If not: stop with **Needs decomposition** and return control to the user/coordinating layer. Do
   not create multiple future local plans. Do not name or call any specific tracker.

## Plan interactively

Prefer the harness native read-only/planning mode when available; otherwise use conversation.

1. Research current Snapshot evidence narrowly (`AGENTS.md`, implicated module README,
   `package-info`/Javadoc, sources/tests, accepted ADRs, conformance). Do not scan the whole repo.
2. Optionally write/update a small local working plan (see [reference.md](reference.md)). Sections
   are optional. No Status field. No plan dependency DAG.
3. Keep the approach visible early. Ask only **material** maintainer decisions (intent, design,
   compatibility, scope). Prefer the harness interactive question facility when available.
4. Harmless local coding choices stay with the implementer later.

## Optional Design Review

Do not run automatically. Recommend when work materially changes public API, compatibility,
package/module responsibility, dependency direction, ownership, hard-to-reverse abstractions,
cross-module design, migration strategy, accepted ADR behavior, or likely new ADR decisions.

Ask whether Design Review is wanted. If yes:

1. Run `implementation-design-review` once (default: one fresh Design Reviewer). Pass a local plan
   path when one exists; for plan-less approach text, embed the design or materialize
   `.agentWork/.session/design-source-<basename>.md`—do not invent a plan file just for review.
2. Present findings; maintainer chooses apply / reject / discuss per finding.
3. Apply only accepted suggestions. Never auto-mutate from reviewer authority.
4. Ask whether another Design Review is wanted. Never auto re-review.

## Optional Plan Review

After the working approach is acceptable to the maintainer, ask whether Plan Review is wanted. If yes:

1. Run `implementation-plan-review` once via a **fresh** reviewer context when the harness supports
   it. Pass requested outcome, acceptance intent, and the local plan / approach source (materialize
   plan-less approach text to `.agentWork/.session/plan-source-<basename>.md` when needed).
2. Present findings; apply / reject / discuss as above. Never mutate the plan from the reviewer.
3. Ask before any another Plan Review. Never auto re-review.

## Build authorization

Neither review transitions to implementation. When planning is sufficient, ask explicitly:

**Build now?**

Only an affirmative maintainer answer proceeds toward `implement-work`. On no, continue refining.
Stop after reporting readiness; do not start implementation unless the user also asked to build.

## Report

Report Needs decomposition or the local plan path (if any), material decisions, optional review
outcomes, and whether Build now was affirmed.
