# Review findings (shared)

Canonical owner of finding severity and stage ownership for planning and implementation reviews.
Planning findings are **recommendations to the maintainer**, not automatic workflow control.

## Finding severity (planning reviews)

### Blocking

The reviewer believes implementation should not proceed without resolving this concern.

This does **not** mean automation is forbidden from proceeding despite explicit maintainer
acceptance. The maintainer may accept a known tradeoff.

### Required

The reviewer believes the plan or design is materially incomplete or incorrect in a way that does
not by itself require a different architecture.

The maintainer may accept the tradeoff knowingly.

### Advisory

Optional improvement, simplification, wording, preference, or over-specification of harmless local
detail. Advisory findings never alone imply the reviewer recommends stopping.

## Exhaustive pass

Inspect the whole relevant artifact before returning an assessment. Return one consolidated set of
material findings. If required evidence is missing, return **Unable to assess** rather than guessing.

## Stage ownership

### Design Review asks

Is the proposed design sound, appropriately simple, compatible, and justified versus credible
simpler alternatives?

### Plan Review asks

Is this local working plan a minimum sufficient aide for the requested outcome without material
guessing or harmful over-specification?

### Implementation Review asks

Does the actual change correctly satisfy the **requested outcome / acceptance intent** and
repository rules?

Implementation Review uses Critical / High / Medium / Low ship severities (see
`implementation-review`). It must not reopen already accepted architecture as taste. Harmless
divergence from a local working plan is not a defect.

## Planning assessments

Prefer:

- **No material concerns**
- **Concerns found**
- **Unable to assess**

Do not treat planning assessments as Pass/Changes-required permission gates into Build.
