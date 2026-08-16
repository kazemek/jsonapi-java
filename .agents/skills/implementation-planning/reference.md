# Implementation planning reference

Use with [SKILL.md](SKILL.md). `AGENTS.md` remains authoritative for ownership and gates.

## Local working plan template

Optional gitignored file under `.agentWork/plans/`. Omit sections that add no value. No Status
field. No plan-level Dependencies.

```markdown
# <Descriptive title>

## Goal

## Current understanding

## Approach

## Constraints

## Checks

## Notes / open questions
```

Local plans orient sessions and aid harness switching. They need not match the final
implementation and are not correctness authority.

## One coherent increment vs Needs decomposition

**One increment:** can be implemented and independently reviewed as one change against repository
reality that exists now.

**Needs decomposition:** later design materially depends on earlier implementation that is not yet
merged. Stop repository planning; decompose in the coordinating layer (when present) or with the
maintainer. Do not invent a future local-plan DAG.

## Material vs local decisions

**Material (pin or ask):** architecture, ownership/source-of-truth, public/observable behavior,
compatibility, migration, module/package boundaries, important diagnostics, required evidence,
acceptance intent.

**Local (leave open):** private helper names, method extraction, ordinary control flow, exact local
sequence, exhaustive file inventories when scope is clear, internal test-helper structure.

## Design Review heuristics

Recommend Design Review when work materially introduces or changes: public API; compatibility
contract; package/module responsibility; dependency direction; ownership; persistent architectural
abstractions; cross-module integration; difficult migration; accepted ADR behavior; likely new ADR.

Straightforward local work following established architecture does not automatically need it.

## Review artifacts

Fixed paths for optional reviews live in
[../implementation-design-review/reference.md](../implementation-design-review/reference.md).

## Normative scenarios

1. Oversized multi-stage feature → Needs decomposition; no multi-plan create.
2. Maintainer rejects a Design finding → continue; severity is advice, not a veto.
3. After accepted review edits → ask before another review; never auto re-review.
4. Maintainer satisfied but answers no to Build now? → stay in planning; do not implement.
5. Direct user outcome with no tracker → valid planning input.
