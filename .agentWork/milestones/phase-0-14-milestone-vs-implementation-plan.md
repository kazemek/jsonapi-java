# Phase 0.14 — Milestone vs Implementation Plan

> **Scope:** Repository workflow
> **Dependencies:** Phase 0.7, Phase 0.11, Phase 0.12, Phase 0.13
> **Status:** Not started

## Goal

Distinguish directional Outlook milestones from size-gated implementation plans so
`implement-milestone` cannot run an umbrella, while keeping Phase 0.13’s design guarantee except
where a plan has a validated inherited-design claim from a Complete contract or from an Outlook
whose design-review Official Pass was produced in the current `milestone-planning` invocation.

## Research and constraints

- [Phase 0.7](phase-0-7-milestone-planning-workflow.md) allowed `Not started` files to be refined
  later and required that an oversized umbrella must not remain an implementable contract
  (`.agents/skills/milestone-planning/reference.md`: rework into implementable files; do not leave
  an umbrella presented as implementable). [Phase 0.12](phase-0-12-milestone-plan-review-workflow.md)
  and [Phase 0.13](phase-0-13-milestone-design-review-workflow.md) then required both reviews on
  every create/refine. Those Complete files stay historical. Live skills change in this follow-up.
- [Phase 0.13](phase-0-13-milestone-design-review-workflow.md) exists because
  `milestone-plan-review` must not judge whether the technical approach is sound
  (`.agents/skills/milestone-plan-review/SKILL.md`). Skipping planning-loop design-review on every
  `Not started` file would allow `new standalone plan → plan-review Pass → implement-milestone`
  with no design check. That path is rejected. `Status: Outlook` alone does not prove a design
  Pass: this change reclassifies 3.2, 4.1, and 4.2 to `Outlook` without design-reviewing them, and
  `.agentWork/.session/` is gitignored (Phase 0.13 non-goal: do not gate `implement-milestone` on
  those artifacts). Inherited design is therefore a **claim that planning validates**, not a Status
  inference.
- Inherited-design **eligibility** (planning-loop design-review skip). A plan qualifies only when
  all of the following hold:
  1. **Explicit claim:** Research contains a markdown link that resolves, relative to the plan
     file or the repository root, to an existing `.agentWork/milestones/*.md` file, plus a
     one-line statement of which source outcome this plan realizes (ports, mirrors, or
     implements). Same-directory hrefs such as `(phase-2-1-jackson-document-codec.md)` qualify.
     No `Parent:` header. Prose phase numbers (`Phase 2.1`) and Dependencies headers are not
     provenance.
  2. **Eligible source state:** the linked file exists and is `Complete`, or is `Outlook` and this
     `milestone-planning` invocation created, refined, or decomposed that Outlook and produced a
     design-review Official Pass for it. Do not Orchestrate design-review of an Outlook this run
     did not write. Prior on-demand Passes, prior planning Passes, and gitignored stubs do not
     count. `Not started` / `In progress` sources never qualify. A citing plan in a run that does
     not write that Outlook is an ineligible claim and follows the no-claim path (unless this
     invocation also refines or decomposes the Outlook).
  3. **Genuine relationship:** the cited source establishes the claimed design outcome (the
     contract being ported, mirrored, or realized). Extra capability the source does not
     establish — new wire rules, extra public flows, or a module/package the source (or an
     accepted ADR it cites) did not already reserve — makes the claim invalid. Jackson-major
     package/API substitution for the same document, mapping, or read/write contract is not
     extra. Phase 2.16’s `jsonapi-java-jackson2` artifact is already reserved by Phase 2.1 /
     ADR-007, so that module registration is not extra relative to the 2.1 writer outcome.
  `milestone-planning` owns routing and this eligibility check. It never grants the skip on an
  invalid claim. `milestone-plan-review` may verify that a *present* claim is accurately documented
  (contract completeness) and must **not** require every plan to claim inheritance. Absence of a
  citation is the normal Phase 0.13 path, not a finding.
- Jackson 2 open files today cite Jackson 3 siblings in prose and Dependencies only: none of
  `phase-2-16`…`phase-2-23` contain a `](phase-…)` milestone-file link. This change adds inspectable
  provenance on qualifying ports (2.16→[2.1](phase-2-1-jackson-document-codec.md),
  2.17→[2.4](phase-2-4-document-reads.md), 2.18→[2.2](phase-2-2-domain-resource-mapping.md),
  2.19→[2.3](phase-2-3-compound-serialization.md), 2.20→[2.8](phase-2-8-sparse-fieldsets.md),
  2.21→[2.9](phase-2-9-jackson3-flat-dto-reader.md),
  2.22→[2.10](phase-2-10-jackson3-domain-envelope.md)). Phase 2.23 must **not** inherit from
  [Phase 2.15](phase-2-15-jackson3-patch-binding.md) while 2.15 is `Not started`. 2.25, 2.26, 3.1,
  3.3–3.5 get no inherited-design citation in this change.
- [Phase 0.11](phase-0-11-implement-milestone-workflow.md) gates the *target* on Status and does
  not gate on gitignored design-review artifacts. Live `implement-milestone` still warns and asks
  on listed dependencies that are not `Complete` (`.agents/skills/implement-milestone/SKILL.md`).
  That warn-and-ask is not a hard block. This follow-up adds two live-skill rules: refuse `Outlook`
  / `Complete` *targets*, and do not proceed when a listed dependency’s Status is `Outlook`
  (`Outlook` means the parent outcome is not delivered). Listed `Not started` / `In progress`
  dependencies keep the Phase 0.11 warn-and-ask so parallel tracks whose Dependencies headers omit
  each other still work. `Outlook → In progress` is invalid. The planning loop is where the design
  guarantee holds.
- Closing an Outlook parent is an explicit `milestone-planning` action so dependents that list it
  (Phase 3.3 lists Phase 3.2) can see `Complete`. Child completion alone is insufficient: listed
  children might omit parent scope, and the parent’s own prerequisites might still be undelivered
  ([Phase 4.2](phase-4-2-stable-release.md) lists [Phase 4.1](phase-4-1-conformance-and-hardening.md);
  after this change both are `Outlook`). Close requires all of: the parent’s canonical child list
  exists and is non-empty; every listed child exists and is `Complete`; those completed contracts
  collectively cover the parent’s Goal / directional deliverables; no unresolved parent scope
  remains; and every **milestone** named in the Outlook parent’s `Dependencies` header is
  `Complete`. Parse that header by collecting phase identifiers (`Phase 3.2`, `Phases 2.1, 2.4`,
  `1.1–3.4`, `3.2, 3.3, and 3.4`). Inclusive ranges expand in **phase order**, not decimal
  order: compare the major phase integer first, then the minor phase integer
  (`2.8 < 2.9 < 2.10 < 2.11`). A range includes every existing `.agentWork/milestones/` file
  whose identifier lies between the endpoints in that order (`1.1–3.4` includes 2.10 and 2.11).
  Missing numbers encountered while expanding a range may be skipped (numbering is sparse). An
  **explicitly named** phase that does not resolve to a milestone file (for example `Phase 3.33`)
  is an error: Close and `implement-milestone` do not proceed, and `milestone-plan-review` treats
  it as a finding — do not skip it. Remaining prose is not a Status gate (4.2’s “verified
  namespace”, 3.2’s Spring Boot line, 3.5’s “Stable … behavior”). An `Outlook` dependency is not
  `Complete`. 4.1’s `Phases 1.1–3.4` therefore must all be `Complete` before 4.1 can Close; 3.5
  names 3.2, 3.3, and 3.4, so `implement-milestone` does not proceed on 3.5 while 3.2 is
  `Outlook`. Close has no warn-and-ask override: `Outlook → Complete` means prerequisite
  outcomes are delivered. No design-review or plan-review on close. Index line becomes the
  historical three-segment Complete form.
  Downstream Dependencies keep pointing at the parent (3.3 stays dependent on 3.2, not on 3.2’s
  children). Files named under an Outlook `## Implementation plans` section must not list that
  Outlook in `Dependencies`; the Research markdown link plus realization statement is parent
  provenance. Only non-child dependents (3.3/3.4 listing 3.2) put the Outlook parent in
  `Dependencies`. Listing the parent on a child would deadlock: `implement-milestone` would not
  proceed, and Close needs those children `Complete`.
- [Phase 0.7](phase-0-7-milestone-planning-workflow.md) and live
  `.agents/skills/milestone-planning/SKILL.md` freeze a contract once implementation has started
  and send new work to a follow-up. `Outlook` never becomes `In progress`, so that freeze does not
  attach to the parent file’s Status. Children inherit the parent Goal/design; a material parent
  rewrite after a child is `In progress` or `Complete` would change the design underneath work
  already started. Outlook mutability: the parent may be materially refined (Goal / directional
  design) only while none of its listed implementation plans is `In progress` or `Complete`. Once
  any listed child has started, that Goal/design is fixed. Planning may still maintain
  `## Implementation plans`, add further implementation plans that satisfy the **existing**
  outcome, and make non-semantic clarifications. A material change to the directional Goal/design
  is a follow-up Outlook with an explicit dependency, not a rewrite of the active parent.
- Canonical child list is a normal Markdown section on the Outlook file, created or updated by
  decompose (including later replacement of a child). Reclassification of 3.2 / 4.1 / 4.2 in this
  change does not invent that section. An Outlook without the section, or with an empty list,
  cannot close.

```markdown
## Implementation plans

- [Phase X.Ya — …](phase-x-ya-….md)
- [Phase X.Yb — …](phase-x-yb-….md)
```

- [AGENTS.md](../../AGENTS.md) currently calls `.agentWork/milestones/` “implementation plans”;
  that sentence is the collapse to undo. Vision remains the product roadmap.
- After this change, Phase 3.2 becomes `Outlook` without being decomposed here. 3.2’s Goal is
  directional WebMVC document transport (media-type, converter, query arguments, safe errors);
  later `## Implementation plans` children are those transport slices, not 3.3/3.4 (already
  sibling dependents). 3.3/3.4 list 3.2, so `implement-milestone` does not proceed on them while
  3.2 is `Outlook`. Phase 3.5 stays a `Not started` decision plan (one go/defer task).
- Same directory and skill filenames. No mass rename of Complete history. Docs/skills-only: no
  repository build, Spotless, or Sonar gates.

## Deliverables

- Extend Status with `Outlook` as the only parent-milestone marker (no `Kind:` header). State
  machines: implementation plan `Not started → In progress → Complete`; milestone
  `Outlook → Complete`. `Outlook` is refinable and decomposable, has no implementation size gate,
  and must still be one coherent directional outcome with a product/architectural boundary that
  later plans can collectively satisfy. An Outlook may be materially refined only while none of
  its listed implementation plans is `In progress` or `Complete`; once any listed child has
  started, the established Goal/design is fixed (list maintenance, additional children for that
  existing outcome, and non-semantic clarifications remain allowed; a material Goal/design
  change is a follow-up Outlook). `implement-milestone` proceeds only for `Not started` /
  `In progress` targets; it refuses `Outlook` and `Complete` targets, never writes `In progress`
  on an `Outlook` file, and does not proceed when a listed dependency is `Outlook`. Listed
  `Not started` / `In progress` dependencies remain warn-and-ask. Decision tickets such as
  Phase 3.5 stay `Not started`. This 0.14 file is an implementation plan (never `Outlook`).
- Split the **planning loop** by Status and validated provenance (`milestone-planning` chooses
  whether to follow design-review Orchestration and/or spawn plan-review; it must not fork
  orchestration text). Overall planning Pass is Pass of the reviews this table required for each
  file in the run; an `Outlook` create/refine does not wait for plan-review:
  - Create/refine `Outlook`: `milestone-design-review` only (no plan-review, no size gate).
    Refuse a material Goal/design refine when any listed child is `In progress` or `Complete`;
    that work is a follow-up Outlook. List maintenance and additional children for the existing
    outcome are not a material refine.
  - Create/refine `Not started` with **no** inheritance claim: `milestone-design-review`, then
    `milestone-plan-review` after design Pass (Phase 0.13). Missing citation is not a finding.
  - Create/refine `Not started` with a **valid** inheritance claim (eligibility above):
    `milestone-plan-review` only. If the source is `Outlook` and this invocation did not
    create/refine/decompose it to an Official Pass, the claim is ineligible (no skip).
  - Create/refine `Not started` with an **invalid** inheritance claim (missing file, ineligible
    lifecycle, Outlook without current-run Pass, source does not establish the claimed outcome,
    or missing/incoherent realization statement): do not grant the skip; correct or remove the
    claim, then route as no-claim (design-review then plan-review) unless a corrected claim
    becomes valid. `milestone-plan-review` flags an inaccurate *remaining* claim; it does not
    demand a claim.
  - Decompose `Outlook`: design-review the parent if this invocation has not already Passed it;
    require Pass; emit `Not started` children that cite the parent in Research and do **not**
    list the parent in `Dependencies`; write/update `## Implementation plans` on the parent;
    plan-review each child only. If any listed child is already `In progress` or `Complete`,
    do not rewrite the parent Goal/design; a design-review `Changes required` that demands a
    material Goal change stops this decompose (follow-up Outlook) instead of editing the
    active parent.
  - Decompose `Not started`: if discovery shows a directional umbrella, set the file to `Outlook`
    **before** emitting children, then follow the `Outlook` decompose path. If it is still one
    implementation-domain plan, merely too large: rewrite the source file into one size-gated
    plan (keep the original phase id for that increment) and create sibling size-gated plans for
    the rest. Each resulting plan is routed as create/refine `Not started` by its own provenance
    (typically no claim → design-review then plan-review; a valid claim → plan-review only). Do
    not treat the split itself as inherited design. Do not leave the oversized source as an
    independently runnable `Not started` contract beside children. An umbrella left as
    `Not started` is a plan-review finding.
  - Close `Outlook`: explicit `milestone-planning` action `Outlook → Complete` only when the
    `## Implementation plans` list exists and is non-empty, every listed child exists and is
    `Complete`, those completed contracts collectively cover the parent’s Goal / directional
    deliverables, no unresolved parent scope remains, and every milestone named in the parent’s
    `Dependencies` header is `Complete`. No warn-and-ask on Close. Update the index to
    three-segment Complete form. No automatic close.
- Keep on-demand `milestone-design-review` available for any open file, including `Not started`
  plans and `Outlook`. On-demand `milestone-plan-review` targets non-`Outlook` open files (does
  not target `Outlook`). On-demand `milestone-review` targets implementation plans (does not
  target `Outlook`). `milestone-handoff` keeps three review kinds: do not suggest plan-review or
  implementation review for `Outlook`.
- Document the two state machines, inherited-design eligibility (Complete durable vs Outlook
  current-run), missing vs invalid provenance, planning-loop vs on-demand design-review, parent
  close (including parent-`Dependencies` Completeness), Outlook mutability after a child starts,
  `## Implementation plans`, open four-segment index (`milestone` iff `Outlook`), and
  Complete three-segment form in `AGENTS.md` and `.agentWork/milestones/README.md`. Preferred
  order stays commentary; Dependencies stay authoritative. `implement-milestone` Status rewrites
  preserve the kind field until `Complete`, then drop to three-segment. `milestone-plan-review`
  enforces that mixed form and, when a provenance claim is present, that it is accurate; it does
  not treat missing provenance as a defect.
- Reclassify open files in place (no Complete-file edits): 3.2, 4.1, and 4.2 become `Outlook`
  without `## Implementation plans` and without being decomposed; 2.15, 2.16–2.23, 2.25, 2.26,
  3.1, 3.3, 3.4, and 3.5 stay non-`Outlook`. Add inherited-design Research citations on 2.16–2.22
  as named above; do not add one on 2.23 to 2.15. Do not add a Jackson 2 parent file or decompose
  3.2 / 4.1 / 2.15 in this change.

## Non-goals

- Renaming files, skills, or Complete milestone contracts.
- A `Kind:` or required `Parent:` header.
- A durable design-review marker or gating any skill on `.agentWork/.session/` artifacts.
- Per-harness model routing or subagent model pinning.
- A new directory or an ADR.
- Changing implementation-review completion gates (second `clean build`).
- Decomposing Phase 3.2, 4.1, or 2.15 in this change.
- Automating `Outlook → Complete` without an explicit `milestone-planning` close.
- Rewriting downstream Dependencies from an Outlook parent to its children.

## Implementation boundaries

- Skills stay under `.agents/skills/` with existing names; change descriptions and routing only.
- `milestone-planning` must not fork design-review orchestration text; it chooses *whether* to
  follow that Orchestration or to spawn plan-review, per Deliverables. It Orchestrates only files
  this run created, refined, or decomposed.
- Provenance syntax is the Research markdown link (relative sibling hrefs included) plus
  one-line realization statement defined above. Planning validates eligibility before choosing
  the skip; plan-review does not invent inheritance requirements.
- `implement-milestone` dependency handling: parse `Dependencies` with the Research
  phase-identifier rule (phase-order ranges, not decimal; sparse range gaps may be skipped;
  an explicit unresolvable phase is an error, not a skip). `Outlook` listed deps do not
  proceed; other non-`Complete` listed deps stay warn-and-ask as in Phase 0.11.
  Implementation-plans children do not list their Outlook parent in `Dependencies`. Close of
  an Outlook has no warn-and-ask and requires every resolved milestone in that parent’s
  `Dependencies` to be `Complete`.
- Complete files and their index lines stay unchanged except inserting this Phase 0.14 entry.

## Test strategy

- Cross-read planning, implement, design-review, plan-review, handoff, and milestone-review skills
  for Status routing, eligibility vs skip, missing vs invalid provenance, pre-decompose Outlook
  normalize, oversized-source rewrite, parent close/coverage/parent-deps, Outlook mutability
  after a child starts, and refuse rules.
- Walk: broad Outlook feature (create → design-review → later decompose with current-run Pass →
  citing children → plan-review → implement → coverage close); standalone plan with no claim;
  proven Complete port (2.16-style); existing Outlook used as provenance (same-run
  refine/decompose Pass, else no-claim path); old `Not started` umbrella normalize; oversized plan
  rewrite with per-file provenance routing and no leftover runnable umbrella; 3.2 → 3.3 via
  parent close; 4.2 cannot close while 4.1 is `Outlook`; 4.1 Close expands `1.1–3.4` in phase
  order (`2.8 < 2.9 < 2.10 < 2.11`; sparse range gaps skipped; explicit missing phase is an
  error); 3.5 names 3.2 so it does not proceed while 3.2 is `Outlook`; Outlook Goal frozen once a
  listed child is `In progress` or `Complete`; 3.5 as a decision plan; 2.23 must not skip
  design-review via 2.15 while 2.15 is `Not started`.
- Confirm open-file Status values, 2.16–2.22 Research links, 2.23 without 2.15 inheritance, and
  open index entries match the reclassification list.
- Docs/skills-only: no repository build, Spotless, or Sonar gates.

## Acceptance criteria

- [ ] Open files 3.2, 4.1, and 4.2 have Status `Outlook` and no `## Implementation plans` section;
      2.15, 2.16–2.23, 2.25, 2.26, 3.1, 3.3, 3.4, and 3.5 are not `Outlook`; 2.16–2.22 Research
      sections each contain a markdown link to the named Complete source file plus a one-line
      realization statement; 2.23 has no inherited-design citation to 2.15; this 0.14 file is
      never `Outlook`; Complete files are unmodified.
- [ ] Every non-`Complete` index line uses `title — milestone|plan — module/scope — status` with
      `milestone` iff that file’s Status is `Outlook`; `implement-milestone` preserves the kind
      field on `In progress` rewrites and drops it on `Complete`; `milestone-plan-review` enforces
      that mixed form.
- [ ] `implement-milestone` refuses `Outlook` and `Complete` targets, implements only
      `Not started` or `In progress`, never sets an `Outlook` file to `In progress`, and does
      not proceed when a listed dependency is `Outlook` (`Dependencies` parsed by the Research
      phase-identifier rule — phase-order ranges, sparse range gaps skipped, explicit missing
      phase is an error — so 3.5 naming 3.2 is listed); listed `Not started` / `In progress`
      dependencies remain warn-and-ask.
- [ ] `milestone-planning` follows the Deliverables routing table, including: no-claim →
      design-review then plan-review (missing citation is not a defect); valid claim →
      plan-review only after eligibility (Outlook sources need a create/refine/decompose Pass
      from this invocation; no side-target Orchestration); invalid claim → no skip; Outlook
      decompose → current-run parent Pass, citing children that do not list the parent in
      `Dependencies`, `## Implementation plans` sync, plan-review children only; `Not started`
      umbrella → `Outlook` before children; oversized plan → rewrite source into a size-gated
      plan plus siblings, each routed by its own provenance (no blanket design-review skip), no
      leftover runnable oversized `Not started` source.
- [ ] `milestone-planning` can close an `Outlook` parent to `Complete` only when
      `## Implementation plans` is non-empty, every listed child exists and is `Complete`, those
      contracts collectively cover the parent’s Goal / directional deliverables, no unresolved
      parent scope remains, and every milestone named in the parent’s `Dependencies` is
      `Complete` (no warn-and-ask; `Dependencies` parsed by the Research phase-identifier rule:
      phase-order ranges such as 4.1’s `1.1–3.4` with `2.8 < 2.9 < 2.10 < 2.11`, sparse range
      gaps skipped, explicit unresolvable phases are errors); dependents that list the parent
      (for example 3.3 → 3.2, and 3.5 naming 3.2) then see a `Complete` dependency or remain
      blocked while it is `Outlook`; a material Outlook Goal/design refine is refused once any
      listed child is `In progress` or `Complete` (follow-up Outlook instead), while list
      maintenance and additional children for the existing outcome remain allowed.
- [ ] On-demand design-review remains available for `Outlook` and for `Not started` plans;
      on-demand plan-review and implementation review do not target `Outlook`; handoff does not
      suggest plan or implementation review for `Outlook`.
- [ ] `AGENTS.md` and `.agentWork/milestones/README.md` document the two state machines,
      inherited-design eligibility, missing vs invalid provenance, parent close (including
      parent-`Dependencies` Completeness), Outlook mutability after a child starts,
      `## Implementation plans`, planning-loop vs on-demand design-review, and the mixed index
      form.
- [ ] Skill filenames are unchanged; Phases 0.7, 0.11, 0.12, and 0.13 files are unchanged.
