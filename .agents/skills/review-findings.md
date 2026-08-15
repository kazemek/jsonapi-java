# Review findings (shared)

Canonical owner of finding severity, exhaustive-pass rules, and design-vs-plan stage ownership for
`implementation-design-review` and `implementation-plan-review`. Orchestration budgets, review
epochs, and dependency waves remain in `implementation-planning`.

## Finding severity

Every actionable finding uses exactly one severity. Reviewers must not label completeness or taste
issues as `Blocking` to force another architectural cycle.

### Blocking

The plan cannot safely be accepted at the **current review stage** without resolving the finding.

Typical `Blocking` design findings:

- architectural contradiction;
- incoherent source-of-truth or ownership model;
- unsafe or impossible migration;
- unresolved public-contract break;
- incompatible dependency assumptions;
- a missing decision that requires choosing among materially different architectures;
- an implementation plan whose design cannot be executed correctly without inventing a new
  architectural decision.

A missing detail that exposes a genuinely unresolved architectural choice is `Blocking`, never
`Required`.

### Required

A concrete correctness or completeness gap that does **not** invalidate an already coherent
architecture.

Typical `Required` findings:

- missing compatibility detail where intended behavior is already clear;
- missing preservation of an existing constructor, factory, or API surface;
- missing test case or invariant;
- insufficiently explicit mapping between already-selected types;
- missing file, scope, or gate detail;
- migration detail an implementation agent must not guess, but which needs no new architectural
  decision.

`Required` findings must be fixed before implementation-plan approval. At design review they do
**not** by themselves cause design failure or another design-review cycle; they carry into plan
review. Planning persists unresolved design and plan gate findings in gate carry-forward artifacts.
Design **Blocking** clear on a later fresh design `Pass` after planning applied them; design
**Required** and plan gate findings stay sticky until plan review verifies they are addressed. Do not
require fresh reviewers to read prior findings merely to clear them.

### Advisory

A non-blocking improvement: naming preference, wording clarification, optional simplification,
stylistic note, or speculative extensibility with no current correctness impact. Advisory findings
never alone fail a review stage.

## Exhaustive pass

Fresh-context reviewers inspect the entire relevant plan (or eligible plan set for their task)
before returning a verdict. Do not stop after the first `Blocking` issue. Return one consolidated
set of all material `Blocking`, `Required`, and `Advisory` findings visible in that pass.

Exhaustive does not mean inventing speculative concerns to increase finding count. If the review
cannot finish because required repository evidence is missing or ambiguous, return `Blocked` with
the reason rather than guessing.

## Stage ownership

### Design review asks

Is the chosen architecture coherent, justified, safe, and compatible with the repository?

Primary concerns: ownership and source-of-truth, architecture boundaries, migration shape,
dependency direction, public-contract consequences, important tradeoffs, and whether material
design decisions remain unresolved.

Design review must not demand every implementable detail before plan review may run. It must not
fail solely on `Required` or `Advisory` completeness issues when the architecture itself is sound.

### Plan review asks

Can an implementation agent execute this plan correctly and completely without material guessing?

Primary concerns: compatibility details, affected files/modules, exact migration obligations,
tests, verification gates, ordering, acceptance criteria, missing implementation-level invariants,
and consistency with an already-approved design.

Plan review must not become a second architecture contest. If a finding shows a genuine unresolved
architectural choice or contradicts an approved design, classify it `Blocking` and require planning
refinement with a new design review — do not hide it as `Required` completeness.

## Design verdict mapping

- **Changes required:** at least one `Blocking` finding.
- **Pass:** no `Blocking` findings. `Required` findings remain in the artifact and in the planning
  gate carry-forward for plan review; `Advisory` findings are residual.
- **Blocked:** a prerequisite is missing or ambiguous; do not guess.

## Plan-review verdict mapping

- **Changes required:** at least one `Blocking` or `Required` finding remains (including unresolved
  `Required` findings carried from a design Pass).
- **Pass:** no `Blocking` or `Required` findings remain. `Advisory` findings may remain.
- **Blocked:** required repository evidence, dependencies, or lifecycle state is unavailable or
  ambiguous.
