# Tracker-agnostic external work coordination

> **Scope:** repository-wide
> **Dependencies:** None
> **Status:** In progress
> **Work item:** KAZ-74

## Goal

Remove Linear-specific operational wording from the committed agent workflow so public
contributors can follow it without any external tracker access, while keeping maintainer-specific
Linear mechanics in a gitignored, on-demand local skill outside the repository contract.

## Research and constraints

- `AGENTS.md` — authoritative workflow/routing/knowledge-ownership rules; currently the only
  committed file asserting Linear as the coordination owner.
- `.agents/skills/implementation-planning/SKILL.md` and `reference.md`, `implement-plan/SKILL.md`,
  `implementation-review/SKILL.md`, `implementation-plan-review/SKILL.md`,
  `implementation-design-review/design.md` and `adversarial.md` — carry negative/procedural Linear
  wording ("Linear is optional coordination", "never use Linear IDs", "update the linked Linear
  item", "do not treat Linear as truth") that is really tracker-agnostic.
- `.agentWork/plans/*.md` — 16 files carry `> Work item: KAZ-XX` optional-traceability lines; these
  stay (changing backlog priorities/relations is a non-goal).
- The current OpenCode harness discovers project `.agents/skills/*/SKILL.md` and routes via each
  skill's `description`; discovery has only been verified in this one harness, so no ambient routing
  hint is added and multi-harness routing remains unverified. `.gitignore` did not ignore that path.

## Deliverables

- Tracker-agnostic wording in `AGENTS.md` and the committed workflow skills listed above.
- Minimal tracker-agnostic lifecycle synchronization hooks in the committed workflow
  (`implementation-planning` and `implement-plan`) for plan materialization, implementation start,
  review start, and finalization.
- A `.gitignore` entry for `.agents/skills/linear-coordination/`.
- A gitignored `.agents/skills/linear-coordination/SKILL.md` carrying the full maintainer Linear
  operating policy and a routing `description` (next-work discovery, future-work capture, backlog
  governance, plan/work-item synchronization, lifecycle synchronization, drift checks).

## Non-goals

- Hardcoding Linear, `KAZ-*`, Linear MCP calls, or Linear statuses into committed skills.
- A committed `AGENTS.local.md` or equivalent public local-extension mechanism.
- GitHub ↔ Linear synchronization automation.
- Changing current backlog priorities, issue relations, or the 16 plan files' work-item lines.
- Modifying the in-flight AGENTS.md / skill-compaction work (already merged).

## Implementation boundaries

- Any remaining `Linear` mention in committed files must be intentionally explanatory rather than
  operational; no committed file may require Linear where a generic external tracker suffices.
- External work-item identifiers remain optional coordination metadata, never repository truth or
  plan identity.
- Tracker unavailability produces an explicit coordination-sync gap, not an engineering-correctness
  failure.

## Test strategy

- Docs/workflow change; no build. Verify by search and link/section consistency review.

## Acceptance criteria

- [ ] `git grep -nE 'Linear|KAZ-' -- AGENTS.md .agents/skills/` returns no matches (the temporary
      plan under `.agentWork/plans/` is out of scope while live and is deleted at finalization).
- [ ] No committed `AGENTS.md` or skill requires or assumes Linear where a generic tracker suffices.
- [ ] Committed workflow expresses only tracker-agnostic lifecycle hooks (materialization,
      implementation start, review start, finalization) with no Linear status/project/label/priority
      names or API/MCP mechanics.
- [ ] `.gitignore` ignores `.agents/skills/linear-coordination/` (verified with `git check-ignore`).
- [ ] Local skill is discovered by the current OpenCode harness (evidence recorded); broader
      multi-harness routing verification remains pending.
- [ ] Fresh `implementation-review` passes against this plan.
- [ ] Applicable completion gates from `AGENTS.md` pass (docs/planning + workflow tiers; no build).
- [ ] Completed plan is deleted (`git rm`) and the deletion pushed before the PR is presented as
      ready for final review.
