---
name: idea-refinement
description: >
  Structured 3-phase ideation: diverge, evaluate, and converge on a solution direction.
  Use before starting any new feature or product idea. Triggers: "new idea", "brainstorm",
  "what should I build", "refine this idea", "explore options".
---

# Skill: Idea Refinement

## Overview

Turns a vague idea into a concrete, validated direction with explicit assumptions surfaced
before any code is written. Produces a one-page idea document as the input to spec-driven development.

## When to Use

- User has a raw feature idea
- Requirements are ambiguous or unclear
- Exploring alternative approaches before committing
- "What should we build?" questions

## When NOT to Use

- Requirements are already fully specified
- Task is a clear bug fix
- Change is < 15 minutes to implement

## 3-Phase Process

### Phase 1: DIVERGE — Explore the Problem Space

Before generating solutions, deeply understand the problem:

1. **Restate the problem** as a How Might We question:
   > "How might we [solve problem] for [user] so they can [outcome]?"

2. **Generate 3–5 variations** exploring different lenses:
   - *Simplification:* What's the absolute minimum that solves the core need?
   - *Inversion:* What if we solved the opposite problem?
   - *Audience shift:* Who else has this problem? How do they solve it?
   - *Combination:* What if we combined this with [adjacent feature]?
   - *Constraint removal:* What if [key constraint] didn't exist?

3. **Surface assumptions:**
   ```
   ASSUMPTIONS I'M MAKING:
   1. [Assumption] — is this correct?
   2. [Assumption] — should we verify this?
   3. [Assumption] — what if this is wrong?
   ```

### Phase 2: EVALUATE — Stress-Test the Options

For each candidate approach:

| Approach | Solves core need? | Implementation effort | Reversibility | Risk |
|---|---|---|---|---|
| A: [name] | ✅/⚠️/❌ | S/M/L | High/Low | [main risk] |
| B: [name] | ... | ... | ... | ... |
| C: [name] | ... | ... | ... | ... |

Questions to challenge each option:
- What breaks if this assumption is wrong?
- What does the user do when this fails?
- What's the second-order effect of this choice?
- Would users actually use this if we built it?

### Phase 3: CONVERGE — Define the Direction

Pick one direction. Write the idea document:

```markdown
# Idea: [Name]

**Problem Statement:**
[Who] cannot [do what] because [root cause]. This causes [consequence].

**Recommended Direction:**
[One sentence describing the chosen approach]

**Why This Approach:**
[2-3 sentences on why this wins over alternatives]

**Key Assumptions:**
1. [Assumption] — [how to validate]
2. [Assumption] — [how to validate]

**MVP Scope (What We're Building):**
- [Feature 1]
- [Feature 2]

**Not Doing (Explicitly Excluded):**
- [Thing that sounds related but isn't in scope]
- [Thing that can be done later]

**Success Metric:**
[How we'll know this solved the problem — measurable]

**Open Questions:**
- [Question that needs answering before implementation]
```

## Anti-Patterns to Avoid

| ❌ Anti-Pattern | ✅ Fix |
|---|---|
| Yes-machine (agree with first idea) | Generate ≥ 3 alternatives |
| Skipping "who is this for" | Always name the user |
| No assumptions surfaced | List assumptions explicitly |
| "We need all the features" | Force a Not-Doing list |
| Solution before problem | Define HMW before generating solutions |
| Vague success criteria | Quantify: "user can do X in < 30 seconds" |

## Output

Save to: `.ai/ideas/YYYY-MM-DD-[idea-name].md`

This document becomes the input to `spec-driven-development`.

## Verification Checklist

- [ ] Problem restated as HMW question
- [ ] ≥ 3 alternative approaches explored
- [ ] Assumptions explicitly listed
- [ ] One recommended direction chosen with rationale
- [ ] Not-Doing list written (as important as what IS in scope)
- [ ] Success metric is measurable
- [ ] Idea document saved to `.ai/ideas/`
