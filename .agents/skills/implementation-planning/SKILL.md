---
name: implementation-planning
description: Lifecycle workflow entrypoint that researches current repository reality, distinguishes facts the agent establishes from material decisions the maintainer owns, and maintains an optional gitignored local working plan for one coherent implementation increment. Supports interactive maintainer planning, bounded planning spikes, and optional Design/Plan Review. Use when the maintainer explicitly requests implementation planning or approach refinement.
disable-model-invocation: true
---

# Implementation Planning

Lifecycle workflow entrypoint for one coherent implementation increment. Uses the capabilities
available in the current session; it does not depend on any particular harness mode. It requires a
minimum capability set to execute reliably and must not silently degrade into a partial version when
a required capability is unavailable. Informal planning and exploration may still happen in any
session using whatever capabilities are available there.

Create or refine optional **local working memory** under gitignored `.agentWork/plans/`. A local
plan is optional, non-authoritative, and not permission to implement. `AGENTS.md` owns repository
authority and completion gates.

Shared finding semantics: [../review-findings.md](../review-findings.md). Template and examples:
[reference.md](reference.md).

## Capabilities

This workflow requires, at minimum, the ability to:
- inspect repository evidence;
- persist gitignored working/session artifacts (`.agentWork/.session/`, and `.agentWork/plans/`
  when used);
- interact with the maintainer for material decisions.

Artifact-write is a required capability even when a particular run creates no persisted artifact; the
workflow guarantees it can persist gitignored artifacts if its execution path requires them. A local
implementation plan itself remains optional.

Additional activities are conditional on capabilities:
- bounded executable spikes require executable capabilities (see Bounded planning spikes);
- optional Design/Plan Review requires the capabilities to run or hand off to the fresh reviewer and
  persist its review artifact.

If a capability required by the core workflow is unavailable, stop the repository workflow and report
the limitation to the maintainer rather than silently running a reduced version. Informal planning may
continue outside the workflow.

## Resolve the requested work

1. Resolve an explicit requested outcome / acceptance intent from the user (or optional
   `.agentWork/.session/work-context.md` when present). A direct user request is sufficient; no
   external tracker is required.
2. Decide whether the work is **one coherent implementation increment** against current repository
   reality (can be implemented and independently reviewed as one change now).
3. If not: stop with **Needs decomposition** and return control to the user/coordinating layer. Do
   not create multiple future local plans. Do not name or call any specific tracker.

## Plan interactively

1. Research current Snapshot evidence narrowly (`AGENTS.md`, implicated module README,
   `package-info`/Javadoc, sources/tests, accepted ADRs, conformance). Do not scan the whole repo.
2. Establish facts and separate them from material decisions the maintainer owns (see
   [reference.md](reference.md) for the facts-vs-decisions rule).
3. Optionally write/update a small local working plan (see [reference.md](reference.md)). Sections
   are optional. No Status field. No plan dependency DAG. Do not treat absence of a local plan file
   as planning failure.
4. Keep the approach visible early. Ask only **material** maintainer decisions (intent, design,
   compatibility, scope), with a brief trade-off and a recommended answer. Harmless local coding
   choices stay with the implementer later.

## Bounded planning spikes

A planning spike is disposable executable work used to answer one concrete technical question. The
output is the answer, not the code. Spikes are conditional on executable capabilities being
available; this workflow must not assume it can change modes to gain them.

A spike:
- starts with one explicit technical question;
- is used only when execution materially reduces uncertainty compared with reasoning or static
  inspection, and only when executable tools are available in the current session;
- lives in an OS temporary directory or under gitignored `.agentWork/.session/spikes/<slug>/`;
- may compile or run small experiments, and may inspect framework/library behavior using repository
  code or dependencies as inputs;
- must not modify production code as part of the experiment;
- is not engineering truth and needs no production-quality architecture, tests, docs, formatting,
  coverage, or other production gates;
- is minimal and disposable, deleted when no longer useful;
- records only the resulting fact or decision in the working plan/report when it matters later.

Make the spike operationally bounded: before executing, state the concrete technical question and the
cheapest decisive experiment; stop as soon as the question is answered or the experiment can no longer
answer it. Do not introduce arbitrary time limits, line-count limits, production-quality requirements,
or lifecycle stages.

If execution would materially reduce uncertainty but execution is unavailable, surface that limitation
to the maintainer rather than inventing a workaround or assuming a mode switch.

Escape condition: if the planner is no longer answering the original technical question and is instead
building production behavior, stop the spike and return to planning. Do not proceed to production
implementation without explicit maintainer request or authorization to enter `implement-work`. A spike
never bypasses implementation authorization.

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
2. Present findings; the maintainer may apply, reject, or discuss findings separately. Plan Review
   must not mutate the plan; accepted edits happen outside the review, then ask before another
   Plan Review if wanted. Never auto re-review.

## Implementation authorization

Neither review transitions to implementation. Planning is never automatic: planning ends ready for
implementation, and implementation begins only when the maintainer explicitly requests or authorizes
the `implement-work` lifecycle workflow. Explain the current approach and readiness, then stop; do not
start implementation without that explicit authorization. The authorization is explicit maintainer
intent, not literal slash syntax; `implement-work` is the repository workflow name for that boundary.

## Report

Report Needs decomposition or the local plan path (if any), the facts established versus the material
decisions resolved, spike outcomes when a spike was used, optional review outcomes, and that planning
is complete and ready for implementation pending explicit `implement-work` authorization.
