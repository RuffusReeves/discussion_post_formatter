**Discussion Post Formatter Project Preliminary Specifications (Updated)**

File Name Date Stamp: 2025-08-29  
Revision Date: 2025-09-02  
Repository: https://github.com/RuffusReeves/discussion_post_formatter  
Current Implemented Scope: Configuration editing (unit/theme) with comment preservation + automatic file content loading for `*_file_address` keys (excluding output).

Naming Convention Note: Filename retains original creation date; revision date inside reflects most recent substantive update.

## 1. Context

The tool is being developed to automate producing an HTML snippet suitable for university discussion forums. Current committed functionality is limited to safe, comment‑preserving updates of `unit` and `theme`, placeholder resolution for `<UNIT_NUMBER>`, and automatic file content loading for qualifying address keys.

## 2. Current Implementation Snapshot

Implemented:
- Config loader preserving comments and blank lines.
- Interactive prompts for `unit` (digits) and `theme` (string).
- Runtime placeholder resolution for `<UNIT_NUMBER>`.
- Automatic derived content loading for `*_file_address` (excluding `output_file_address`).
- Safe persistence of modified keys only.

Not Yet Implemented (originally described below; pending):
- Source file aggregation and compilation.
- Runtime execution and output capture.
- HTML block construction templates.
- Syntax highlighting / theme file consumption.
- Inline code detection in text blocks.
- User text overrides & conditional file rewrites.
- HTML tidy / beautification.
- Multi-block orchestration (assignment, explanations, question, references).
- Theming application beyond stored scalar.

Sections below retain aspirational design with status labels.

---

## 3. Expectations for discussion_post_formatter (Annotated)

1. Unit + theme prompt with persisted update.  
   Status: DONE (basic prompt + persist; no theme-based logic yet)

2. Package selection `cs_1102_base.Unit_<UNIT_NUMBER>`.  
   Status: PENDING

3. Replace wildcard source specification with explicit file enumeration.  
   Status: PENDING

4. Code collection.  
   Status: PENDING

5. Compilation & diagnostics capture.  
   Status: PENDING

6. Runtime execution & output capture.  
   Status: PENDING

7. Theming selection menu & style application.  
   Status: PENDING (theme value stored only)

8. Inline highlighting in non-code blocks.  
   Status: PENDING

9. Explanation blocks ingestion & override logic.  
   Status: PENDING

10. Full HTML snippet assembly (single artifact).  
    Status: PENDING

11. HTML Tidy / formatting pass.  
    Status: PENDING

12. Structured block templates & substitution logic.  
    Status: PENDING

13. ContentBlock orchestration pipeline.  
    Status: PENDING

14. Theming spacing / style quirks handling.  
    Status: PENDING

15. Question block logic (with assignment text snippet).  
    Status: PENDING

16. References block integration.  
    Status: PENDING

17. CLI flags for non-interactive overrides.  
    Status: PENDING

## 4. Revised Near-Term Milestones (M1–M10)

M1 Minimal HTML output (static template, placeholder resolution only).  
M2 File content ingestion (textual blocks) with fallback notices (extends current loading with robustness).  
M3 Java source enumeration + compilation + runtime capture.  
M4 Basic regex-based Java highlighter (keywords, strings, comments).  
M5 Theme file loader & inline style mapping integration.  
M6 Modular ContentBlock architecture & HtmlAssembler.  
M7 User override prompts for text inputs (optional persistence).  
M8 Question & references block integration.  
M9 HTML tidy / formatting pass.  
M10 Test suite expansion (Config + placeholder + highlighter + assembly).

## 5. Stage ↔ Milestone Mapping (Summary)

| Stage | Description (Tech Spec) | Milestone(s) |
|-------|--------------------------|--------------|
| 0 | Config editing + placeholder | (Pre-M1 – DONE) |
| 1 | Minimal HTML output | M1 |
| 2 | Text block embedding | M2 |
| 3 | Compilation + runtime capture | M3 |
| 4 | Syntax highlighting | M4 |
| 5 | Theming application | M5 |
| 6 | ContentBlock refactor & assembly | M6 |
| 7 | Inline code detection & overrides | M7 |
| 8 | Tidy / formatting | M9 (after M8) |
| 9 | Test expansion & hardening | M10 |

(M8 integrates additional blocks; it straddles Stages 6–7 logically.)

## 6. Risk Notes (Updated)

- Documentation drift: Mitigated by explicit status labels (review after each milestone).  
- Complexity creep: Keep M1 minimal (no highlighting / execution).  
- Inline formatting complexity: Defer advanced parsing until after stable HTML skeleton.  
- Academic integrity: Non-generative scope preserved.

## 7. Next Actions (Concrete)

1. Implement M1 (produce minimal HTML file using existing config paths—if file paths missing, placeholders or notices).  
2. Add file existence checks + fallback placeholders.  
3. Draft theme file schema (JSON) for early feedback.  
4. Introduce logging abstraction (info/warn/error).  

## 8. Naming & Conventions

- Placeholders: Always angled uppercase `<UNIT_NUMBER>`; additional tokens require explicit approval.  
- Derived keys: `snake_case_file_address` → `camelCaseFileContents` (suffix `Contents`).  
- Class names: `HtmlAssembler`, `ThemeLoader`, `ExecutionService`.

## 9. Out of Scope (Current Cycle)

- Multi-language source highlighting (only Java planned initially)  
- Network operations  
- GUI or web interface  
- Database persistence

## 10. Change Log

- 2025-09-02: Added derived file loading to implemented scope, clarified mapping table, standardized naming, risk mitigation update.  
- 2025-09-01: Status annotations added; alignment with README.  
- 2025-08-29: Initial draft.

## 11. Appendix: Acceptance Criteria (Selected Milestones)

- M1: Running tool produces `unit_<UNIT_NUMBER>_discussion_post.html` containing a header with resolved unit and no unresolved `<UNIT_NUMBER>` tokens.  
- M2: Missing input files yield placeholder blocks (e.g. “(assignment text missing)”) without exceptions.  
- M3: Compilation failures captured and displayed in a Compiler Messages section; runtime optional if success.  
- M4: At least keywords, strings, comments wrapped in `<span style="">` with distinct theme styles.  

(End of document)