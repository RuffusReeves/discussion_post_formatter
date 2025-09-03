# Discussion Post Formatter - Functional Specification

## 1. Document Control
1.1 Version: 0.2.0  
1.2 Status: In Progress (core HTML generation, highlighting & execution implemented; CLI / extended theming pending)  
1.3 Owner: RuffusReeves  
1.4 Reviewers: (Self / Peers)  
1.5 License: MIT (intended; LICENSE file pending)  
1.6 Repository: https://github.com/RuffusReeves/discussion_post_formatter  
1.7 Last Sync With Tech Spec: 2025-09-03 (Tech Spec 0.2.0)

## 2. Overview
2.1 Purpose: Generate a formatted, self‑contained HTML discussion post consolidating assignment text, explanations, highlighted source code, compiler messages, runtime output, discussion question, and references.  
2.2 Current Scope (implemented):
- Interactive editing of `unit` and `theme` with comment‑preserving persistence
- Runtime resolution of `<UNIT_NUMBER>` (non-persistent)
- Automatic loading of input file contents for keys containing `address` (excluding `output_file_address`) into derived, non-persisted camelCase variables
- HTML artifact generation with ordered sections and inline-only styling
- Inline code formatting (backticks, triple backticks, `<code>` tags)
- Java source syntax highlighting via internal `Highlighter`
- Optional compilation & execution of assignment Java source; inclusion of previous and fresh run outputs
- Separate sections for compiler messages, previous output, current execution output
2.3 Planned Scope Extensions: CLI flags (non-interactive mode), richer theming (external JSON integration), structured logging, diff/patch preview mode, multi-language highlighting, placeholder expansion.  
2.4 Out of Scope (current & near term): Web UI, database persistence, network I/O, multi-user concurrency, cloud storage.  
2.5 Stakeholders: Student author (primary), peer reviewers / instructors (consumers).  
2.6 References: Course instructions; Java SE docs; style & academic integrity guidelines.

## 3. Goals
| Goal | Description | Status |
|------|-------------|--------|
| G1 | Reduce manual formatting time to < 3 minutes from prepared inputs | PARTIAL (pipeline exists; timing not yet measured) |
| G2 | Provide deterministic, consistent section ordering | ACHIEVED |
| G3 | Enable peer review in a single HTML artifact | ACHIEVED (initial version) |
| G4 | Preserve user edits & explanatory comments in config | ACHIEVED |
| G5 | Provide traceable build/log steps | PARTIAL (stdout only; no structured logs) |

## 4. System Context
4.1 Planned Inputs: `config.txt`, assignment / explanation / question / references text files, Java source(s), (future) theme JSON.  
4.2 Current Inputs: `config.txt` + any file at keys containing `address` (except output) auto‑loaded into memory.  
4.3 Planned Output: HTML file + (optional) auxiliary logs / diff previews.  
4.4 Current Output: Single inline‑styled HTML file + console diagnostics.  
4.5 External Tools (now used): `javac`, `java` (single-file compile & run).  
4.6 Constraints: Local filesystem, one unit at a time, Java 17+.  
4.7 Assumptions: UTF-8 text files; code file is compilable in isolation; no module system complexity.

## 5. Functional Requirements

Legend: Implemented | Partial | Pending

| ID | Requirement | Status | Notes |
|----|-------------|--------|-------|
| FR-01 | Load key/value pairs from `config.txt` (preserve comments & blanks) | Implemented | Ordered internal model |
| FR-02 | Resolve `<UNIT_NUMBER>` in values at runtime (non-persistent) | Implemented | Only placeholder supported |
| FR-03 | Structured ingestion & classification of assignment, intro, discussion question, references, sample code as typed blocks | Implemented | Derived keys drive section assembly |
| FR-04 | Compile unit source file(s) and capture compiler messages | Implemented | Single target file; simple output |
| FR-05 | Execute target main class and capture runtime output | Implemented | Captures stdout; exit code noted |
| FR-06 | Separate compiler diagnostics from runtime output | Implemented | Distinct HTML sections |
| FR-07 | Apply syntax highlighting using selected theme | Implemented | Internal palette only; external JSON pending |
| FR-08 | Assemble ordered HTML sections (all block types) | Implemented | Deterministic ordering |
| FR-09 | Write final HTML artifact to output path | Implemented | Inline styles only |
| FR-10 | Console override of text block contents (optional persistence) | Pending | Backlog / FR extension |
| FR-11 | Log defaults / fallbacks and decisions (structured) | Pending | Needs logging abstraction |
| FR-12 | Store theme selection for future styling | Implemented | Scalar config key |
| FR-13 | Auto-load `address` file contents (excluding `output_file_address`) as derived camelCase values | Implemented | Generic ingestion |
| FR-14 | Provide CLI flags `--unit`, `--theme`, `--out` for non-interactive mode | Pending | Planned |
| FR-15 | Insert placeholder notices when configured input files are missing | Partial | Warning only; placeholder messaging TODO |
| FR-16 | Validate unit input (digits only); retain previous value if invalid | Implemented | Regex check |
| FR-17 | Restrict automatic ingestion to keys ending `_file_address` (avoid unintended reads) | Pending | Policy not enforced yet |
| FR-18 | Inline code (backticks/triple backticks / `<code>`) formatting | Implemented | InlineCodeProcessor |
| FR-19 | Include both previously captured and fresh execution outputs | Implemented | Dual output sections |
| FR-20 | Expose compilation failure details with clear prefix | Partial | Basic text only; structured formatting planned |
| FR-21 | HTML escaping for all user content & code blocks | Implemented | Escapes in processors |
| FR-22 | Theme JSON parsing fallback to internal defaults | Partial | ThemeLoader loads; highlight palette still internal |
| FR-23 | Provide execution toggle (CLI or config) | Pending | Hard-coded flag currently |
| FR-24 | Diff / patch mode for previewing changes to HTML | Pending | Future milestone |

## 6. Improvement Backlog (Unscoped)
BL-01 Additional placeholders (`<THEME>`, `<COURSE_CODE>`)  
BL-02 Markdown (full) parsing / list & heading normalization  
BL-03 HTML tidy / beautifier pass (post-generation)  
BL-04 Accessibility: semantic tags, ARIA landmarks, contrast audit  
BL-05 Alternate export (Markdown summary / PDF)  
BL-06 Multi-language syntax highlighting hook interface  
BL-07 Config-driven section ordering customization  
BL-08 Execution timeout & sandbox controls  

## 7. Data Model
Current:
- Config: Ordered lines (ENTRY / COMMENT / BLANK) + `Map<String,String>` raw + derived file contents map
- Highlighter: Internal token categories (keyword, type, number, string, comment)
- InlineCodeProcessor: Regex-based transformation pipeline (inline & block)
- Execution: Simple compile + run; returns concatenated stdout (stderr folded into compile failure text)
Future:
- ContentBlock(type, content, metadata)
- ExecutionResult { compilerMessages[], runtimeStdout, runtimeStderr, exitCode, timestamp }
- Theme { name, background, foreground, tokenStyles }
- AssemblyContext { unit, themeName, blocks, generationTime, sourcePaths }

## 8. Interface (CLI)
Current Flow:
1. Load `config.txt`
2. Display current unit/theme
3. Prompt for new unit & theme; validate unit digits
4. Save if changed; reload
5. Show raw vs resolved config snapshot, derived content preview
6. Generate HTML (auto-run compile & execute) and write output

Planned Additions:
- CLI flags override prompts (`--unit`, `--theme`, `--no-exec`, `--no-highlight`)
- Missing file summary table before generation
- Confirmation prompt if critical inputs missing
- Structured exit codes (0 success, non-zero warnings/errors)

## 9. File & Key Conventions
- Keys: `snake_case`
- Derived naming: `some_input_file_address` → `someInputFileContents`
- Placeholder format: `<UPPERCASE_WITH_UNDERSCORES>` (only `<UNIT_NUMBER>` now)
- Proposed constraint: Only load keys ending `_file_address` (FR-17)
- Removal strategy: future soft-remove marks as commented line

## 10. Non-Functional Requirements
| NFR | Description | Status | Notes |
|-----|-------------|--------|-------|
| NFR-01 | Stage 1 runtime < 1s typical without execution | Achieved | Small projects |
| NFR-02 | Full pipeline (single file compile+run) < 3s typical | Achieved (informal) | Measure formally |
| NFR-03 | Portability: Windows/macOS/Linux (JDK 17+) | Partial | Only basic path ops tested |
| NFR-04 | HTML readability (tidy pretty-print) | Pending | Planned post builder extraction |
| NFR-05 | Academic Integrity (formatting only) | Achieved | No generation of answers |
| NFR-06 | Graceful handling of missing files | Partial | Warnings only |
| NFR-07 | Maintainability: core config module < 300 LOC | Achieved | Within target |
| NFR-08 | Traceability: FR ↔ milestone mapping | Partial | Table present; milestones informal |

## 11. Error Handling
Current:
- Malformed config line (no '=') => treated as comment
- Invalid unit => notice + revert to prior
- Missing input file => console warning
- Compilation failure => returned text block labeled "[Compilation failed]"
- Execution non-zero exit => note appended to output block
Planned:
- Distinct stderr capture section
- Placeholder HTML block for each missing content piece
- Execution timeout safeguard
- Logging abstraction for machine-readable diagnostics (JSON lines)

## 12. Theming
Current:
- Scalar `theme` passed directly to internal `Highlighter` palette
- ThemeLoader parses JSON for potential future mapping (not yet integrated into highlight style selection)
Planned:
- Palette resolution order: External JSON → Internal defaults
- `<THEME>` placeholder support in paths (if justified)
- User-extensible token style map with graceful fallback

## 13. Security / Academic Notes
- Local-only file operations
- No network access, secrets, or remote execution
- Code execution restricted to configured single Java file directory
- Emphasis: Artifact aggregates existing educational content; must not fabricate solutions

## 14. Testing Strategy
Current Gaps: No automated tests yet.
Planned Test Matrix:
- T1 Config round-trip preserves comments & order
- T2 `<UNIT_NUMBER>` substitution resolution
- T3 Derived file content loading (naming + error handling)
- T4 Missing file placeholder insertion (after implementation)
- T5 Compilation & execution pipeline (success / failure / exit code)
- T6 Highlighter token classification coverage
- T7 HTML assembly ordering invariance snapshot
- T8 Theme fallback logic (JSON vs default)
- T9 InlineCodeProcessor transformations (backticks, triple backticks, nested edge cases)
- T10 CLI flag overrides (non-interactive mode)
- T11 Performance baseline (<3s target with execution)
- T12 HTML escaping correctness (malicious input injection tests)

## 15. Risks
| ID | Risk | Impact | Mitigation |
|----|------|--------|------------|
| R1 | Documentation drift | Medium | Update spec in same commit as feature |
| R2 | Broad ingestion heuristic reads unintended files | Low | Enforce `_file_address` suffix (FR-17) |
| R3 | Execution hangs | Medium | Add timeout / future kill-switch |
| R4 | Cross-platform path differences | Low | Add tests across OS if feasible |
| R5 | Palette & theme divergence | Low | Unify via mapping layer |
| R6 | Large input performance degradation | Low | Future caching & streaming |
| R7 | Insufficient test coverage delaying refactors | Medium | Prioritize T1–T7 early |

## 16. Open Issues
| ID | Issue | Status |
|----|-------|--------|
| OI-01 | Main class detection strategy (explicit vs scan) | Open |
| OI-02 | Placeholder expansion beyond `<UNIT_NUMBER>` | Deferred |
| OI-03 | Logging abstraction choice (custom vs library) | Open |
| OI-04 | External theme JSON to Highlighter mapping | Open |
| OI-05 | Execution timeout & interruption handling | Open |

## 17. Change Log
0.2.0 Added HTML generation, syntax highlighting, inline code processing, compile & run integration (FR-04–07, FR-18, FR-19, FR-21). Updated goals statuses; added FR-20–24.  
0.1.2 Split FR-03 scope; added FR-13 (auto ingestion), FR-16 (validation), FR-17 (future restriction). Clarified implemented vs pending ingestion.  
0.1.1 Updated spec to reflect minimal implemented state (config editing only).  
0.1.0 Initial scaffold (aspirational list).

## 18. Appendix
A1 Example config excerpt:
```
# Core Settings
unit = 3
theme = tango
assignment_text_file_address = ../assignments/assignment_text.txt
discussion_question_file_address = ../assignments/discussion_question.txt
code_file_address = ../project/src/Unit_<UNIT_NUMBER>/Discussion_Assignment.java
output_file_address = ../assignments/unit_<UNIT_NUMBER>_discussion_post.html
```
A2 Representative directory layout:
```
src/main/java/formatter/
  Config.java
  DiscussionPostFormatter.java
  Highlighter.java
  InlineCodeProcessor.java
  Utils.java
  ThemeLoader.java
  Theme.java
config.txt
assignments/
themes/
```
A3 Section order (current deterministic):
1 Title
2 Introduction
3 Assignment Overview
4 Primary Explanation (+ Additional)
5 Discussion Question Context
6 Discussion Question
7 Sample Code
8 Assignment Code
9 Compiler Messages
10 Previously Captured Output
11 Current Execution Output
12 References
13 Footer

---
(Functional Specification 0.2.0 – reflects state as of 2025-09-03)