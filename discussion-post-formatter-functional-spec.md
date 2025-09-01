# Discussion Post Formatter - Functional Specification

## 1. Document Control
1.1 Version: 0.1.1  
1.2 Status: In Progress (initial config interaction implemented; core formatting pipeline pending)  
1.3 Owner: RuffusReeves  
1.4 Reviewers: (Classmates / Self)  
1.5 License: MIT (intended)  
1.6 Repository: https://github.com/RuffusReeves/discussion_post_formatter  

## 2. Overview
2.1 Purpose: (Planned) Generate a formatted HTML discussion post consolidating assignment text, explanations, highlighted source, compiler messages, runtime output, discussion question, and references.  
2.2 Current Scope (implemented): Interactive editing of unit and theme values with comment‑preserving configuration persistence.  
2.3 Planned Scope: Local Java console tool reading configured file paths and producing a single HTML artifact per unit.  
2.4 Out of Scope (current & near term): Web UI, database persistence, multi-language parsing.  
2.5 Stakeholders: Student author (primary), peers reviewing code/output (future).  
2.6 References: Course unit instructions, Java SE docs.

## 3. Goals
G1 Reduce manual formatting time (NOT YET ACHIEVED).  
G2 Provide consistent academic presentation (NOT YET ACHIEVED).  
G3 Enable peer review in a single artifact (NOT YET ACHIEVED).  
G4 Preserve user edits to config while retaining explanatory comments (ACHIEVED).  

## 4. System Context
4.1 Inputs (planned): config.txt + assignment/reference/explanation text files + Java source.  
4.2 Current Inputs: config.txt only.  
4.3 Output (planned): HTML file at configured output path.  
4.4 Current Output: Console diagnostic output only (no HTML generation yet).  
4.5 External Tools (future): javac, java.  
4.6 Constraints: Local filesystem, single unit at a time.  
4.7 Assumptions: JDK installed; UTF‑8 files (future phases).

## 5. Functional Requirements (Planned vs Status)
| ID | Requirement | Status |
|----|-------------|--------|
| FR-01 | Load key/value pairs from config.txt (with comments & blanks preserved) | Implemented |
| FR-02 | Replace <UNIT_NUMBER> token in configured paths at runtime | Implemented (single token) |
| FR-03 | Read assignment, intro, discussion, references, sample code files | Pending |
| FR-04 | Compile unit source files and capture compiler messages | Pending |
| FR-05 | Execute target main class and capture output | Pending |
| FR-06 | Capture & store compiler diagnostics separately from runtime output | Pending |
| FR-07 | Apply syntax highlighting using selected theme | Pending |
| FR-08 | Assemble ordered HTML sections | Pending |
| FR-09 | Write final HTML to output path | Pending |
| FR-10 | Allow console override of text block contents (persist optionally) | Pending |
| FR-11 | Log which defaults / fallbacks were used | Pending |
| FR-12 | Provide selectable themes (affecting highlighting CSS inline) | Partially (theme value stored only) |

## 6. Improvement Backlog (abridged)
See separate backlog / wishlist (to be externalized).  
- Multi-token support (<THEME>)  
- Robust file discovery (no wildcards in config)  
- CLI flag overrides (--unit, --theme, --out)  
- Markdown / inline code parsing  
- HTML tidy / beautifier integration  

## 7. Data Model (Current / Future)
Current:
- Config: Ordered list of lines (ENTRY / COMMENT / BLANK) + key/value map.
Future:
- ContentBlock (type, content, metadata)
- Theme model (token → inline style mapping)
- ExecutionResult (compilerMessages[], runtimeOutput)

## 8. Interface (CLI)
Current Flow:
1. Load config.txt
2. Show current unit and theme
3. Prompt for new unit (digits) and theme (string)
4. Persist changes (comments preserved)
5. Print raw vs resolved config snapshot

Planned Additions:
- Prompts for optional overrides of textual sections
- Theme selection from enumerated themes directory
- Summary of resolved file paths
- Confirmation before HTML write

## 9. File Conventions
- config.txt: snake_case keys; angle-bracket placeholder <UNIT_NUMBER>.
- Comments begin with # or //; preserved verbatim.
- Future output path may embed <UNIT_NUMBER>.

## 10. Non-Functional Requirements (Target)
NFR-01 Execution time < 3s typical (pending).  
NFR-02 Portability: JDK 17+ (current code: standard Java only).  
NFR-03 Readability: HTML pretty print (pending).  
NFR-04 Academic Integrity: Tool formats only, no content generation (guiding principle).  
NFR-05 Reliability: Graceful fallbacks for missing files (pending).  

## 11. Error Handling (Current vs Planned)
Current:
- Malformed config line without '=' treated as comment (silent).
Planned:
- Missing file → placeholder text + log
- Compilation failure → still produce HTML with error section
- Runtime exception → captured in Program Output section

## 12. Theming
Current:
- Theme is a stored scalar value only (no expansion/no styling effect yet).
Planned:
- themes/<name>.json or .txt defining token → inline style fragments
- Fallback to default if missing
- Future placeholder <THEME> optional in paths

## 13. Security / Academic Notes
- Local file I/O only; no network operations.
- Avoid secrets in config values.
- Planned footer note reinforcing authorship.

## 14. Testing Strategy (Planned)
T1 Load & preserve comments/blank lines (partially manually verified).  
T2 Placeholder substitution test for <UNIT_NUMBER>.  
T3 Fallback when key missing (future).  
T4 Integration test for full HTML pipeline (future).  

## 15. Risks (Current Relevance)
R1 Overpromising features in docs vs code reality (mitigated by this update).  
R2 Config token drift (<UNIT_NUMBER> vs alternative forms) — stabilized on <UNIT_NUMBER>.  
R3 Scope creep before minimal HTML generation baseline.

## 16. Open Issues (Revalidated)
- OI-01, OI-02 etc. from earlier draft now deferred until core formatter exists.
- Need to identify actual current code gaps after introducing formatting classes.

## 17. Change Log
0.1.1 Updated spec to reflect actual implemented state (config editing only).  
0.1 Initial scaffold (aspirational feature list).

## 18. Appendix
A1 Current config example (excerpt):
```
# Core Settings
unit = 3
theme = default
output_file_address = ../assignments/unit_<UNIT_NUMBER>_discussion_post.html
```
A2 Planned directory layout (subject to refinement).