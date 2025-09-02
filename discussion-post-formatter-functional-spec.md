# Discussion Post Formatter - Functional Specification

## 1. Document Control
1.1 Version: 0.1.2  
1.2 Status: In Progress (config interaction implemented; core formatting pipeline pending)  
1.3 Owner: RuffusReeves  
1.4 Reviewers: (Self / Peers)  
1.5 License: MIT (intended; LICENSE file pending)  
1.6 Repository: https://github.com/RuffusReeves/discussion_post_formatter  
1.7 Last Sync With Tech Spec: 2025-09-02 (Tech Spec 0.1.2)

## 2. Overview
2.1 Purpose: Generate a formatted HTML discussion post consolidating assignment text, explanations, highlighted source, compiler messages, runtime output, discussion question, and references.  
2.2 Current Scope (implemented):  
- Interactive editing of `unit` and `theme` with comment‑preserving persistence  
- Runtime resolution of `<UNIT_NUMBER>` (non-persistent)  
- Automatic loading of input file contents for keys containing `address` (excluding `output_file_address`) into derived variables  
2.3 Planned Scope: Produce a single HTML artifact per unit from configured text/code inputs.  
2.4 Out of Scope (current & near term): Web UI, database persistence, multi-language parsing, network I/O.  
2.5 Stakeholders: Student author (primary), peers (future).  
2.6 References: Course instructions; Java SE docs.

## 3. Goals
G1 Reduce manual formatting time to < 3 minutes from prepared inputs (NOT YET ACHIEVED)  
G2 Provide deterministic, consistent section ordering (NOT YET ACHIEVED)  
G3 Enable peer review in a single HTML artifact (NOT YET ACHIEVED)  
G4 Preserve user edits & explanatory comments in config (ACHIEVED)  
G5 Provide traceable build/log steps (NOT YET ACHIEVED)

## 4. System Context
4.1 Planned Inputs: `config.txt`, assignment / explanation / question / references text files, Java sources.  
4.2 Current Inputs: `config.txt` + any present `*_file_address` (or broadly `address`) files auto‑loaded.  
4.3 Planned Output: HTML file at configured output path.  
4.4 Current Output: Console diagnostic (no HTML file yet).  
4.5 External Tools (future): `javac`, `java`.  
4.6 Constraints: Local filesystem, one unit at a time.  
4.7 Assumptions: JDK 17+, UTF‑8 files.

## 5. Functional Requirements

Legend: Implemented | Pending | Partially

| ID | Requirement | Status | Notes |
|----|-------------|--------|-------|
| FR-01 | Load key/value pairs from `config.txt` (preserve comments & blanks) | Implemented | Ordered model |
| FR-02 | Resolve `<UNIT_NUMBER>` in values at runtime (non-persistent) | Implemented | Sole placeholder |
| FR-03 | Structured ingestion & classification of assignment, intro, discussion question, references, sample code as typed blocks | Pending | Raw file ingestion already handled by FR-13 |
| FR-04 | Compile unit source files and capture compiler messages | Pending | M3 |
| FR-05 | Execute target main class and capture runtime output | Pending | M3 |
| FR-06 | Separate compiler diagnostics from runtime output | Pending | M3 |
| FR-07 | Apply syntax highlighting using selected theme | Pending | M4/M5 |
| FR-08 | Assemble ordered HTML sections (all block types) | Pending | Foundation at M1, full by M6 |
| FR-09 | Write final HTML artifact to output path | Pending | M1 |
| FR-10 | Console override of text block contents (optional persistence) | Pending | M7 |
| FR-11 | Log defaults / fallbacks and decisions | Pending | Needs logging abstraction |
| FR-12 | Store theme selection for future styling | Implemented | Scalar only |
| FR-13 | Automatically load file contents for keys containing `address` (excluding `output_file_address`) into derived, non-persisted variables | Implemented | Generic ingestion; naming converts `address→contents` + camelCase |
| FR-14 | Provide CLI flags `--unit`, `--theme`, `--out` for non-interactive mode | Pending | Backlog |
| FR-15 | Insert placeholder notices when configured input files are missing | Pending | M2 |
| FR-16 | Validate unit input (digits only); retain previous value if invalid | Implemented | Regex `^[0-9]+$` |
| FR-17 | Restrict automatic ingestion to keys ending `_file_address` (to avoid unintended reads) | Pending | Tighten current broad heuristic |

## 6. Improvement Backlog (Unscoped)
BL-01 Additional placeholders (`<THEME>` etc.) gated by explicit need  
BL-02 Markdown / inline code parsing  
BL-03 HTML tidy / beautifier integration  
BL-04 Accessibility enhancements (semantic headings / ARIA)  
BL-05 Alternate export (Markdown summary)  

## 7. Data Model
Current:
- Config: Ordered lines (ENTRY / COMMENT / BLANK) + map key→value; derived contents map key→String (camelCase).
Future:
- ContentBlock(type, content, metadata)
- Theme model (token→inline style)
- ExecutionResult(compilerMessages[], runtimeOutput)
- AssemblyContext(unit, theme, blocks, timestamps)

## 8. Interface (CLI)
Current Flow:
1. Load `config.txt`  
2. Show current unit & theme (raw)  
3. Prompt for new unit (digits) & theme (string)  
4. Persist changes (preserve comments / order)  
5. Display raw vs resolved snapshot (resolved substitutes `<UNIT_NUMBER>`)  
6. Display derived content previews (truncated)  

Planned Additions:
- Existence summary & missing file notices
- Theme selection from directory enumeration
- Confirmation before HTML write
- CLI flags bypassing prompts
- Override prompts for text sections

## 9. File & Key Conventions
- Keys: `snake_case`
- Derived variable naming: `snake_case_file_address` → `snake_case_file_contents` → camelCase (e.g. `assignmentTextFileContents`)
- Placeholder format: `<UPPERCASE_WITH_UNDERSCORES>`; currently only `<UNIT_NUMBER>`
- Comments: lines starting with `#` or `//`
- Planned tightening: restrict auto-ingestion to `_file_address` suffix (FR-17)

## 10. Non-Functional Requirements
NFR-01 Stage 0 runtime < 1s typical  
NFR-02 Full pipeline (≤10 Java files) < 3s typical (target)  
NFR-03 Portability: Windows/macOS/Linux (JDK 17+)  
NFR-04 HTML readability: pretty-print after tidy (post M9)  
NFR-05 Academic Integrity: Formatting only—no answer synthesis  
NFR-06 Reliability: Missing files handled gracefully (post M2)  
NFR-07 Maintainability: Core config module < 300 LOC (current ~)  
NFR-08 Traceability: Every FR mapped to milestone/stage

## 11. Error Handling
Current:
- Malformed lines (no `=`) treated as comments
- Invalid unit input ignored with notice; prior value kept
- Missing file: stderr warning; no placeholder block insertion yet

Planned:
- Missing file → placeholder block + log
- Compilation failure → compiler messages section; runtime skipped
- Runtime exception → abbreviated stack trace block
- Theme missing → fallback to default theme + warning
- Ingestion restriction to `_file_address` only (prevents misreads)

## 12. Theming
Current: Scalar `theme` key only.  
Planned: `themes/<name>.json` or `.properties`; fallback chain; optional `<THEME>` placeholder (backlog).

## 13. Security / Academic Notes
- Local-only I/O
- Execution scope limited to expected unit sources
- No network operations
- Planned footer attribution to reinforce authorship

## 14. Testing Strategy
T1 Config round-trip preserves comments/order (partially manually verified)  
T2 `<UNIT_NUMBER>` substitution test  
T3 Derived file content loading correctness (naming & truncation preview)  
T4 Missing file placeholder insertion (post M2)  
T5 Compilation + runtime capture (M3)  
T6 Syntax highlighting token coverage (M4/M5)  
T7 HTML assembly ordering invariance (M6)  
T8 Theme fallback resolution (M5)  
T9 End-to-end HTML snapshot diff (post M6)  
T10 CLI flag overrides (FR-14)

## 15. Risks
R1 Documentation drift — mitigated by versioned updates per milestone  
R2 Scope creep pre-M1 — enforce minimal acceptance criteria  
R3 Broad “address” heuristic ingests unintended keys — resolved by FR-17  
R4 Cross-platform newline/encoding issues — add tests T1/T3  
R5 Token proliferation — require explicit approval process

## 16. Open Issues
OI-01 Main class detection strategy (explicit config vs scan)  
OI-02 Placeholder expansion beyond `<UNIT_NUMBER>` (decision deferred)  
OI-03 Logging abstraction choice (simple wrapper vs library)  

## 17. Change Log
0.1.2 Split FR-03 scope; added FR-13 (auto ingestion), FR-16 (validation), FR-17 (future restriction); clarified current vs pending ingestion.  
0.1.1 Updated spec to reflect minimal implemented state (config editing only).  
0.1 Initial scaffold (aspirational list).

## 18. Appendix
A1 Example config excerpt (illustrative values):
```
# Core Settings
unit = 3
theme = default
output_file_address = ../assignments/unit_<UNIT_NUMBER>_discussion_post.html
assignment_text_file_address = ../assignments/assignment_text.txt
```
A2 Planned directory layout (to follow with M1/M2).
