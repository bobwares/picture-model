# Picture Model Project - Claude Instructions

## 🚨 CRITICAL: Pre-Execution Requirements 🚨

**These steps are MANDATORY before responding to ANY user request.**

### Phase 1: Load Agentic Pipeline (REQUIRED)

**Action Required:** Read the agentic AI pipeline framework.

**File to Read:**
```
/Users/bobware/ai-projects/agentic-ai-pipeline/AGENTS.md
```

**What This Does:**
- Defines turn-based execution model
- Specifies session context variables
- Provides execution plans
- Establishes governance rules

**Verification:** After reading, you should understand:
- Turn lifecycle
- How to create ADRs
- Branch naming conventions
- Commit message format

---

### Phase 2: Load Project Context (REQUIRED)

**Files to Read in Order:**
1. `./AGENTS.md` - Agent-specific rules for this project
2. `./rules.md` - Coding standards
3. `./truths.md` - Core architectural principles

---

### Phase 3: Initialize Project Structure (if missing)

**Required Directory Structure:**
```
picture-model/
├── ai/
│   ├── context/
│   │   └── project_context.md
│   └── agentic-pipeline/
│       ├── turns/
│       │   └── 1/
│       └── turns_index.csv
```

**If these directories don't exist, create them before proceeding.**

---

## ✅ Pre-Flight Checklist

Before responding to the user, confirm:

- [ ] Read codex-agentic-ai-pipeline/AGENTS.md
- [ ] Read project AGENTS.md, rules.md, truths.md
- [ ] Understood turn-based execution
- [ ] Know the current TURN_ID (default: 1)
- [ ] Identified application pattern (nextjs-spring)
- [ ] Ready to create session context

**IF ANY ITEM IS UNCHECKED, STOP AND COMPLETE IT NOW.**

---

## Execution Model

All work follows the agentic pipeline:

1. **Turn Start:** Write session context
2. **Execute Tasks:** Follow execution plan
3. **Generate Artifacts:** ADR, pull request template
4. **Update Index:** Add entry to turns_index.csv
5. **Commit:** Use governance-compliant commit message

---

## Emergency Override

If the pipeline is preventing critical bug fixes or urgent work, you may proceed WITHOUT the pipeline, but you MUST:
1. Notify the user you're bypassing the pipeline
2. Document why in a comment
3. Retroactively create pipeline artifacts after the urgent work

---

**IMPORTANT:** This is not a suggestion. This is a blocking requirement.
Proceeding without loading the pipeline will result in non-compliant code generation.
