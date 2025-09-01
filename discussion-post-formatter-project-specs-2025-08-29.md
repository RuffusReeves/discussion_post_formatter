**Discussion Post Formatter Project Preliminary Specifications (Updated)**

Revision Date: 2025-09-01  
Repository: https://github.com/RuffusReeves/discussion_post_formatter  
Current Implemented Scope: Configuration editing (unit/theme) with comment preservation.

## Context

The tool is being developed to automate producing an HTML snippet suitable for University of the People discussion forums. Current committed functionality is limited to safe, comment‑preserving updates of unit and theme in config.txt; the HTML assembly, code compilation, and highlighting pipeline are not yet implemented.

## Current Implementation Snapshot

Implemented:
- Config loader preserving comments and blank lines.
- Interactive prompts for unit (digits) and theme (string).
- Runtime placeholder resolution for <UNIT_NUMBER>.
- Safe persistence of modified keys only.

Not Yet Implemented (originally described below but pending):
- Source file aggregation and compilation.
- Runtime execution and output capture.
- HTML block construction templates.
- Syntax highlighting / theme file consumption.
- Inline code detection in text blocks.
- User text overrides & conditional file rewrites.
- HTML tidy / beautification.
- Multi-block orchestration (assignment, explanations, question, references).

Sections below retain the aspirational design but are now annotated with status labels.

---

## Assignments (Conceptual Model – Pending)

Two assignment types per unit (Programming vs Discussion). The formatter will focus on the Discussion artifact (Pending).

## Expectations for discussion_post_formatter (Annotated)

1. Unit + theme prompt with persisted update.  
   Status: PARTIALLY DONE (prompt + persist). No further processing yet.

2. Package selection cs_1102_base.Unit_<UNIT_NUMBER>.  
   Status: PENDING (no code enumeration/compilation yet).

3. Replace wildcard source specification with explicit file enumeration.  
   Status: PENDING.

4–7. Code collection, compilation, execution, highlighting, theming selection menu.  
   Status: PENDING (only theme value storage exists).

8. Inline highlighting in non-code blocks.  
   Status: PENDING.

9. Explanation blocks (file vs console input).  
   Status: PENDING.

10. Full HTML snippet assembly with required inline styles only.  
    Status: PENDING.

11. HTML Tidy application.  
    Status: PENDING.

12–13. Structured block templates and content substitution logic.  
    Status: PENDING (design retained for future build).

14–17. Theming, spacing quirks handling, question + references block logic.  
    Status: PENDING.

## Revised Near-Term Milestones

M1 Minimal HTML output using static template + resolved placeholders.  
M2 File content ingestion (assignment / code / outputs) without compilation.  
M3 Java source enumeration + compilation + runtime capture.  
M4 Basic regex-based Java highlighter (keywords, strings, comments).  
M5 Theme file loader and inline style mapping.  
M6 Modular ContentBlock architecture & HtmlAssembler.  
M7 User override prompts for text inputs with persistence decision.  
M8 Reference & question block integration.  
M9 HTML tidy / formatting pass.  
M10 Test suite introduction (Config + placeholder + highlighter).

## Risk Notes (Updated)
- Documentation currently ahead of code (mitigated by status annotations).
- Potential complexity creep around inline formatting; keep MVP narrow.
- Ensure academic integrity by limiting generation to formatting only.

## Next Actions (Concrete)
1. Implement M1 (produce a minimal HTML file using existing config paths).
2. Introduce ContentBlock abstraction early to avoid monolithic builder.
3. Add simple file existence checks + fallback notices.
4. Draft theme file schema (even if not consumed yet).

This document will be re-versioned once M1 reaches implementation.