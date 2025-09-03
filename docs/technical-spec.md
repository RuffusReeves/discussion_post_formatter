# Discussion Post Formatter - Technical Specification

Version: 0.2.0  
Date: 2025-09-03  
Repository: https://github.com/RuffusReeves/discussion_post_formatter  
Sync: Functional Spec 0.2.0

## Reality vs Plan

This specification distinguishes CURRENT IMPLEMENTATION from PLANNED DESIGN to remain truthful and prevent architectural drift.

---

## 1. Overview

CURRENT:
- Java console utility that edits and persists `unit` and `theme` in `config.txt` (comment & blank-line preservation).
- Resolves `<UNIT_NUMBER>` placeholder at access time (never persisted back).
- Automatically loads file contents for keys containing `address` (excluding `output_file_address`) into transient derived camelCase variables.
- Generates a self‑contained inline‑styled HTML discussion post aggregating intro, assignment text, explanations, discussion question, code (sample + assignment), compiler messages, prior captured program output, fresh execution output, references.
- Performs Java source syntax highlighting via internal `Highlighter`.
- Formats inline code constructs (single/backtick pairs, triple backticks, `<code>` tags) using `InlineCodeProcessor`.
- Optionally compiles & runs the configured assignment Java file (simple stdout capture).

PLANNED (BEYOND CURRENT):
- Modular ContentBlock system & HtmlAssembler to replace monolithic builder.
- CLI flags / non-interactive execution (`--unit`, `--theme`, `--no-exec`, `--no-highlight`, `--out`).
- Structured logging abstraction and richer diagnostic panels (stderr, timing).
- Full external theme JSON → highlight style mapping and palette unification.
- Diff/patch preview mode for regeneration deltas.
- Multi-language highlighting hook and advanced placeholder expansion.

---

## 2. Architecture

### 2.1 Implemented Components

| Component | Responsibility | Notes |
|-----------|----------------|-------|
| `DiscussionPostFormatter` | Orchestrates config load/update, theme choice, HTML assembly, optional compile/run, file write | HTML generation currently lives here (to be refactored) |
| `Config` | Ordered parse of `config.txt`; preserves comments/blanks; placeholder resolution; derived file loading | Derives camelCase `...Contents` |
| `Highlighter` | Lightweight Java-ish tokenizer + inline `<span style>` emission | Internal theme palette (keywords, strings, comments, numbers, types) |
| `InlineCodeProcessor` | Transforms inline and fenced code regions to styled `<code>` / `<pre>` blocks | Regex-based; escapes HTML |
| `ThemeLoader` | Parses theme JSON (name, description, colors, token styles) | Styles not yet wired to `Highlighter` palette |
| `Theme` | Immutable theme data holder | Future mapping target |
| `Utils` | File IO helpers, Java compilation & execution (`javac` + `java`), existence checks, extension utility | Minimal error structuring |
| (Derived Keys Mechanism) | Converts `*_file_address` entries (and any `address`-containing key except output) into memory-resident contents | Will be narrowed by FR-17 |

### 2.2 Partially Implemented

| Component | Partial Aspect | Missing |
|-----------|----------------|---------|
| Execution Path | Single-file compile & run | Multi-file scanning, timeout, stderr panel |
| Theme Handling | Internal palette selection + JSON parsing | Dynamic palette from JSON, fallback cascade |

### 2.3 Planned / Not Yet Implemented

| Planned Component | Purpose |
|-------------------|---------|
| `ContentBlock` model | Encapsulate section data & metadata for assembly |
| `HtmlAssembler` | Deterministic layout & templating; facilitates testing |
| `Logger` abstraction | Structured (JSON line) + human logs (levels: info/warn/error) |
| `ExecutionService` (refined) | Timeout, stderr capture, exit code classification |
| `FileCollector` | Enumerate multiple source files (secure traversal) |
| `HtmlTidyIntegration` | Optional pretty-print / normalization |
| CLI Argument Parser | Non-interactive & automation support |
| PlaceholderExpander | Whitelisted multi-token resolution (`<THEME>`, etc.) |
| Diff Generator | Produce stable patch for regenerated HTML |

Grouping:
- Rendering: Highlighter, ThemeLoader, InlineCodeProcessor, (future HtmlAssembler)
- Execution: Utils (current), future ExecutionService + FileCollector
- Configuration: Config + derived key logic
- Post-processing: (future) HtmlTidyIntegration
- Support: Logger, PlaceholderExpander

---

## 3. Configuration Format

CURRENT EXAMPLE:
```
# Core Settings
unit = 3
theme = tango
output_file_address = ../assignments/unit_<UNIT_NUMBER>_discussion_post.html
assignment_text_file_address = ../assignments/assignment_text.txt
```

CURRENT RULES:
- Lines beginning with `#` or `//` preserved verbatim.
- Only `<UNIT_NUMBER>` placeholder supported; resolved at runtime.
- Malformed lines without `=` become comments (prevents data loss).
- Key parse: first `=` splits key/value; remainder preserved in value.
- Keys are `snake_case`; values trimmed outer whitespace only.
- Derived content rule: any key containing substring `address` (except `output_file_address`) → file read.
- Naming transformation: `snake_case_file_address` → `snakeCaseFileContents` (replace `address` with `contents`, then camelCase).

LIMITATIONS:
- Broad heuristic may load unintended keys containing `address`.
- No explicit error classification (warnings printed to stdout).

PLANNED ADJUSTMENTS:
- Restrict ingestion to keys ending exactly `_file_address` (FR-17).
- Introduce explicit whitelist for placeholders.
- Optional config keys: `main_class`, `execute`, `highlight`, `log_level`, `section_order`.

---

## 4. Processing Workflow (Stages & Status)

| Stage | Description | Status (0.2.0) |
|-------|-------------|---------------|
| 0 | Config editing + placeholder resolution | COMPLETE |
| 1 | Minimal HTML output scaffold | COMPLETE |
| 2 | Embed text blocks & fallbacks | COMPLETE (basic warnings; placeholder text TODO) |
| 3 | Compilation + runtime capture | COMPLETE (simple) |
| 4 | Syntax highlighting | COMPLETE (internal palette) |
| 5 | Theme style application (external) | PARTIAL (JSON parsed; not bound) |
| 6 | ContentBlock & HtmlAssembler refactor | PENDING |
| 7 | Inline code + user overrides | COMPLETE (inline code); overrides PENDING |
| 8 | Tidy / formatting pass | PENDING |
| 9 | Test suite expansion & robustness | PENDING |

---

## 5. Theming

CURRENT:
- Config `theme` value selects internal palette in `Highlighter`.
- ThemeLoader parses JSON (`name`, `description`, `background`, `foreground`, `styles`) for informational use.
- No dynamic injection of JSON token styles into highlight decisions yet.

PLANNED:
- Palette resolution chain: External JSON (exact token) → External JSON defaults (fallback keys) → Internal palette default.
- `<THEME>` placeholder for path expansion (deferred).
- User-defined additional token groups (annotation, directive) once stable.

---

## 6. Syntax Highlighting

CURRENT IMPLEMENTATION:
- Token categories: KEYWORD, TYPE (simple allowlist), STRING, COMMENT (single + multi-line), NUMBER, IDENT, SYMBOL, WHITESPACE.
- Input is HTML-escaped before tokenization to prevent markup injection.
- Output wraps styled tokens with `<span style="...">`; others appended raw.
- Themes provide color roles: background, text, keyword, string, comment, number, type.

LIMITATIONS:
- No char literal differentiation (treated like strings if in quotes).
- No annotation-specific styling.
- Generic types / angle brackets not semantically interpreted.
- Numeric literal variants (hex, binary) not distinguished.

PLANNED IMPROVEMENTS:
- Annotation token recognition (`@Ident`).
- Optional performance optimization (single pass state machine).
- Multi-language strategy via pluggable tokenizers.

---

## 7. HTML Assembly

CURRENT:
- Built inline in `DiscussionPostFormatter` (string builder).
- Strict deterministic section order:
  1 Title
  2 Introduction
  3 Assignment Overview
  4 Primary Explanation (+ Additional Explanation)
  5 Discussion Question Context
  6 Discussion Question (blockquote)
  7 Sample Code
  8 Assignment Code
  9 Compiler Messages
  10 Previously Captured Program Output
  11 Current Execution Output
  12 References
  13 Footer

- Inline styles only (no `<style>` or classes).
- Escaping performed for injected dynamic text (double escaping avoided for already processed inline code).

PLANNED:
- `ContentBlock` abstraction to allow reordering, conditional omission, templated transforms.
- `HtmlAssembler` with testable `render(List<ContentBlock>)` method.
- Optional formatting pass (pretty-print) behind a flag.

---

## 8. Error Handling Strategy

CURRENT:
- Missing input file: console warning; section may show placeholder text or be omitted.
- Compilation failure: prefixed text block (“[Compilation failed] ...”).
- Non-zero runtime exit: appended note with exit code.
- Invalid unit input: ignored with user notice.
- Theme JSON parse failure: logged to console; fallback internal palette.

PLANNED:
- Distinct stderr capture section.
- Placeholder HTML panels with standardized style for each missing critical input.
- Execution timeout & interrupt hook.
- Structured log entries: `{timestamp, level, code, message, context}`.

---

## 9. Testing Plan

CURRENT AUTOMATION: None (manual verification).

PLANNED TEST MATRICES:
- Config:
  - Preservation: Input = Output (except updated keys).
  - Placeholder substitution correctness.
  - Derived key naming transforms.
- Highlighting:
  - Representative token classification snapshot.
  - Edge cases: nested comments (invalid patterns), escaped quotes.
- InlineCodeProcessor:
  - Single vs triple backticks, overlapping patterns, HTML escaping.
- Execution:
  - Compilation success, failure (syntax error), runtime exception, non-zero exit.
- HTML Assembly:
  - Section ordering invariance.
  - Escaping of special chars (<, >, &, quotes).
  - Presence/absence logic when files missing.
- Performance:
  - Generation under specified size constraints (< ~3s including compile).
- Regression:
  - Snapshot diff on golden HTML output.

---

## 10. Performance Considerations

CURRENT SCALE ASSUMPTION:
- ≤ 1 primary Java source file (current run target).
- ≤ 50 KB text inputs per section.

CURRENT COMPLEXITY:
- Highlighting: O(n) characters.
- Config loading: O(lines).
- Execution: External process invocation overhead dominates.

PLANNED:
- Caching derived file contents if multi-pass assembly emerges.
- Optional skip-execution flag for faster runs.

---

## 11. Security / Academic Integrity

- Local-only operations; no network or dynamic code injection outside provided Java file.
- Execution scope limited to resolved code file path.
- HTML output includes authorship attribution footer (basic).
- Formatting tool; does not synthesize academic answers (integrity statement retained in docs).

---

## 12. Open Technical Decisions

| Topic | Status | Notes |
|-------|--------|-------|
| Theme file format | JSON selected (implementation partial) | Supports nested token maps |
| Logging | OPEN | Likely simple facade first |
| Placeholder expansion | DEFERRED | Prevent uncontrolled token proliferation |
| Main class detection | OPEN | Will likely use explicit config key (`main_class`) |
| HTML pretty-print | OPEN | Evaluate minimal in-house vs external lib |
| CLI parsing | OPEN | Simple args inspection vs small library |
| Diff generation method | OPEN | Potential: textual diff on HTML vs last build snapshot |

---

## 13. Change Log (Tech Spec)

0.2.0 Added implemented highlighting, inline code processing, HTML generation, compile/run integration; updated staging table; introduced FR expansions (execution dual outputs, theme partial).  
0.1.2 Added derived file loading details; refined component grouping; clarified error handling.  
0.1.1 Segregated implemented vs planned; minimal config-focused scope.  
0.1.0 Initial aspirational draft.

---

## 14. Next Engineering Tasks (Actionable)

1. Extract HTML assembly into `HtmlAssembler` + `ContentBlock` models.
2. Introduce CLI flags (`--no-exec`, `--no-highlight`, `--unit`, `--theme`, `--out`).
3. Implement structured stderr capture & distinct panel.
4. Integrate ThemeLoader styles into Highlighter palette selection.
5. Add execution timeout (watchdog) & exit code classification.
6. Establish initial JUnit tests (Config preservation, highlighting snapshot, inline processing).
7. Restrict derived loading to `_file_address` suffixed keys.
8. Add placeholders for missing sections with standardized styling.

---

## 15. Appendix

A. Example Generated HTML (Truncated Top):
```
<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>
<title>Unit 3 Discussion Post</title><meta name='viewport' content='width=device-width,initial-scale=1'>
</head><body style="font-family:Arial,Helvetica,sans-serif;line-height:1.5;margin:2rem;">
<h2 ...>Unit 3 Discussion Post</h2>
...
</body></html>
```

B. Internal Highlighter Theme Example:
```
tango:
  background: #f8f8f8
  keyword: #204a87
  string:  #c41a16
  comment: #8f5902
```

C. Derived Key Naming Example:
`assignment_text_file_address` → `assignmentTextFileContents`

D. Inline Code Patterns Supported:
- Single backticks: `code`
- Triple backticks (fenced): ```block```
- `<code>inline</code>`

E. Placeholder Roadmap:
- Current: `<UNIT_NUMBER>`
- Proposed (deferred): `<THEME>`, `<COURSE_CODE>`

---

(End Technical Specification 0.2.0 – reflects state as of 2025-09-03)