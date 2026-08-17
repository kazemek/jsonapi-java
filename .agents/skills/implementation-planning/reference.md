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

## Facts vs decisions

**Facts** are primarily the agent's responsibility. The agent establishes relevant facts through
available evidence, or explicitly reports when a material fact could not be established (rather than
asserting fabricated certainty). Relevant facts include current repository behavior, existing
conventions, library/framework capabilities, external API behavior, whether an approach compiles or
executes, and consequences observable through tests or a bounded experiment.

Establish facts in investigation order: 1) repository evidence; 2) primary documentation/specification;
3) a bounded executable spike when execution is available and materially useful. Do not ask the
maintainer what investigation can answer.

**Material product/design decisions** belong to the maintainer: architecture, ownership/source-of-truth,
public/observable behavior, compatibility, migration, module/package boundaries, significant scope
trade-offs, acceptance intent. When asking a material decision, explain the trade-off briefly and
provide a recommended answer plus rationale.

**Local (leave open to the implementer):** private helper names, method extraction, ordinary control
flow, exact local sequence, exhaustive file inventories when scope is clear, internal test-helper
structure, and other choices that do not materially affect the requested outcome or repository
contracts.

## Bounded planning spikes

A planning spike is disposable executable work used to answer one concrete technical question; the
output is the answer, not the code. Use one only when an explicit question exists, execution materially
reduces uncertainty, and executable capabilities are available in the current session. State the
concrete question and the cheapest decisive experiment before executing; stop as soon as it is answered
or can no longer be answered. Keep the spike in an OS temp dir or `.agentWork/.session/spikes/<slug>/`, do
not modify production code, and do not treat it as engineering truth. If a required capability is
unavailable, surface the limitation rather than inventing a workaround. Escape condition: if the spike
stops answering the original question and becomes production implementation, stop and return to planning;
production implementation begins only with explicit maintainer authorization to enter `implement-work`.

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
4. Maintainer satisfied but has not authorized `implement-work` → stay in planning; do not implement.
5. Direct user outcome with no tracker → valid planning input.
6. Planning completes → do not automatically start implementation; await explicit maintainer
   authorization to enter `implement-work`.
