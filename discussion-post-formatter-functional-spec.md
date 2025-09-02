# Discussion Post Formatter - Functional Specification

## 1. Document Control
1.1 Version: 0.1.2  
1.2 Status: In Progress (initial config interaction implemented; core formatting pipeline pending)  
1.3 Owner: RuffusReeves  
1.4 Reviewers: (Self / Peers)  
1.5 License: MIT (intended; LICENSE file pending)  
1.6 Repository: https://github.com/RuffusReeves/discussion_post_formatter  
1.7 Last Sync With Tech Spec: 2025-09-02 (Tech Spec 0.1.2 equivalent revision)

## 2. Overview
2.1 Purpose: Generate a formatted HTML discussion post consolidating assignment text, explanations, highlighted source, compiler messages, runtime output, discussion question, and references.  
2.2 Current Scope (implemented): Interactive editing of `unit` and `theme` values with comment‑preserving configuration persistence; runtime resolution of `<UNIT_NUMBER>`; automatic loading of input file contents for `*_file_address` keys (excluding output).  
2.3 Planned Scope: Local Java console tool producing one HTML artifact per unit.  
2.4 Out of Scope (current & near term): Web UI, database persistence, multi-language parsing, network I/O.  
2.5 Stakeholders: Student author (primary), peers (future).  
2.6 References: Course instructions, Java SE docs.

## 3. Goals
G1 Reduce manual formatting time (Target: < 3 minutes from inputs to HTML) – NOT YET ACHIEVED  
G2 Provide consistent academic presentation (Target: deterministic section ordering) – NOT YET ACHIEVED  
G3 Enable peer review in a single artifact – NOT YET ACHIEVED  
G4 Preserve user edits to config while retaining explanatory comments – ACHIEVED  
G5 Provide traceable build steps in logs (info/warn/error) – NOT YET ACHIEVED  

## 4. System Context
4.1 Planned Inputs: `config.txt`, assignment/reference/explanation/question text files, Java source, compiler/run outputs.  
4.2 Current Inputs: `config.txt` + any present `*_file_address` text inputs (loaded automatically).  
4.3 Planned Output: HTML file at configured output path.  
4.4 Current Output: Console snapshot only (no HTML).  
4.5 External Tools (future): `javac`, `java`.  
4.6 Constraints: Local filesystem, single unit at a time.  
4.7 Assumptions: JDK 17+, UTF‑8 encoding.  

## 5. Functional Requirements

| ID | Requirement | Status | Notes |
|----|-------------|--------|-------|
| FR-01 | Load key/value pairs from `config.txt` with comments & blanks preserved | Implemented | Ordered line model |
| FR-02 | Replace `<UNIT_NUMBER>` token in configured paths at runtime (non-persistent) | Implemented | Only token supported |
| FR-03 | Read assignment, intro, discussion question, references, sample code files | Pending | After M2 |
| FR-04 | Compile unit source files and capture compiler messages | Pending | M3 |
| FR-05 | Execute target main class and capture runtime output | Pending | M3 |
| FR-06 | Capture & store compiler diagnostics separately from runtime output | Pending | M3 |
| FR-07 | Apply syntax highlighting using selected theme | Pending | M4/M5 |
| FR-08 | Assemble ordered HTML sections | Pending | M1 foundation; full by M6 |
| FR-09 | Write final HTML to output path | Pending | M1 |
| FR-10 | Allow console override of text block contents (persist optionally) | Pending | M7 |
| FR-11 | Log defaults / fallbacks used | Pending | Introduce logging first |
| FR-12 | Store theme value for future styling | Implemented | Scalar only |
| FR-13 | Automatically load file contents for keys ending `_file_address` (excluding output) into transient derived content variables | Implemented | Creates e.g. `assignmentTextFileContents` |
| FR-14 | Provide CLI flags `--unit`, `--theme`, `--out` for non-interactive runs | Pending | Backlog |
| FR-15 | Insert placeholder notices when configured input files are missing | Pending | M2 |
| FR-16 | Validate unit input (digits only) and retain previous value if invalid | Implemented | Regex: `^[0-9]+$` |

## 6. Improvement Backlog (Unscoped / Not Yet FR)
BL-01 Multi-token placeholder support (`<THEME>`, etc.)  
BL-02 Markdown inline code parsing  
BL-03 HTML tidy / beautifier integration  
BL-04 Accessibility tags (ARIA / semantic landmarks)  
BL-05 Export alternate formats (Markdown summary)  

## 7. Data Model
Current:
- Config: Ordered list of lines (ENTRY / COMMENT / BLANK) + map of key → value; derived content map (key → fileContent).
Future:
- ContentBlock (type, content, metadata)
- Theme model (token → inline style mapping)
- ExecutionResult (compilerMessages[], runtimeOutput)
- AssemblyContext (unit, theme, blocks, timestamps)

## 8. Interface (CLI)
Current Flow:
1. Load `config.txt`.
2. Show current unit & theme (raw).
3. Prompt for new unit (digits) and theme (string); blank = keep.
4. Persist changes (comments & order preserved).
5. Print raw vs resolved config snapshot (resolved shows `<UNIT_NUMBER>` substituted).

Planned Additions:
- Prompts for optional overrides of specific text sections.
- Theme selection from enumerated themes directory.
- Summary of resolved file paths and existence check results.
- Confirmation before HTML write.
- CLI flags to skip interactive prompts.

## 9. File Conventions
- Keys: `snake_case`.
- Derived variable naming: `snake_case_file_address` → `camelCaseFileContents`.
- Placeholder format: `<UPPERCASE_WITH_UNDERSCORES>`.
- Current valid placeholder: `<UNIT_NUMBER>` only.
- Comments: lines starting with `#` or `//`.
- Newline normalization: preserve existing line endings; future test ensures idempotence on Windows & Unix.

## 10. Non-Functional Requirements (Targets)
NFR-01 Execution time (Stage 0) < 1s typical on modest hardware.  
NFR-02 Execution time (full pipeline up to M3) < 3s with ≤ 10 Java files.  
NFR-03 Portability: Works on Windows, macOS, Linux (JDK 17+ only).  
NFR-04 Readability: Final HTML passes pretty-print (post M9).  
NFR-05 Academic Integrity: Tool does not autonomously synthesize substantive assignment answers.  
NFR-06 Reliability: Missing file fallback (no uncaught exceptions) (from M2).  
NFR-07 Maintainability: Config parsing core < 300 LOC, isolated.  

## 11. Error Handling
Current:
- Malformed line without `=` treated as comment (no crash).
- Invalid unit input (non-digit) ignored; prior value retained with notification.

Planned:
- Missing file → placeholder notice + log (no abort).
- Compilation failure → HTML includes compiler messages; runtime skipped.
- Runtime exception → captured stack trace (trimmed) included in output section.
- Theme missing → fallback to default theme; log warning.
- HTML write failure → descriptive error; no partial file left behind.

## 12. Theming
Current:
- Stored scalar `theme` only (no stylistic effect).

Planned:
- `themes/<name>.json` listing style tokens.
- Fallback / inheritance rule: missing token → default theme token.
- Possible `<THEME>` placeholder (backlog; not scheduled).

## 13. Security / Academic Notes
- Local file I/O only.
- No network or remote code execution.
- Execution restricted to enumerated unit source directory.
- Future: optional footer attribution.

## 14. Testing Strategy
T1 Config round-trip preserves comments & order (Implemented partial manual).  
T2 Placeholder substitution `<UNIT_NUMBER>` correct (Planned).  
T3 Derived file content loading accurate (Planned).  
T4 Missing file fallback insertion (M2).  
T5 Compilation + runtime capture integration (M3).  
T6 Syntax highlighting token coverage (M4/M5).  
T7 HTML assembly ordering invariance (M6).  
T8 Theme fallback resolution (M5).  
T9 End-to-end snapshot diff (post M6).  

## 15. Risks
R1 Documentation drift vs code reality – mitigate with versioned spec updates per milestone.  
R2 Scope creep pre-M1 – enforce minimal acceptance criteria.  
R3 Token proliferation – require explicit approval before new placeholders.  
R4 Platform-specific path/encoding edge cases – add normalization tests (T1/T3).  

## 16. Open Issues
OI-01 Decide strategy for main class detection for execution (explicit config vs scan).  
OI-02 Placeholder strategy beyond `<UNIT_NUMBER>` (defer until concrete need).  
OI-03 Define logging abstraction (slf4j vs simple wrapper).  

## 17. Change Log
0.1.2 Added FR-13, FR-16, clarified goals & test IDs, derived content loading documented, acceptance metrics for G1–G3.  
0.1.1 Updated spec to reflect actual implemented state (config editing only).  
0.1 Initial scaffold (aspirational feature list).

## 18. Appendix
A1 Example current config excerpt (values illustrative):
```
# Core Settings
unit = 3
theme = default
output_file_address = ../assignments/unit_<UNIT_NUMBER>_discussion_post.html
```
A2 Planned directory layout (subject to refinement) to be introduced with M1/M2.