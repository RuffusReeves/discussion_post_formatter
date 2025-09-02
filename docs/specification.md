# Discussion Post Formatter - Technical Specification (Updated)

Version: 0.1.2  
Date: 2025-09-02  
Repository: https://github.com/RuffusReeves/discussion_post_formatter  
Sync: Functional Spec 0.1.2

## Reality vs Plan

This specification separates CURRENT IMPLEMENTATION from PLANNED DESIGN to remain truthful.

## 1. Overview

CURRENT:
- Java console utility for editing and persisting `unit` and `theme` in `config.txt` with comment preservation.
- Resolves `<UNIT_NUMBER>` placeholder in values at access/display time (never written back).
- Automatically loads file contents for keys ending in `_file_address` (except `output_file_address`) into transient derived camelCase variables.

PLANNED:
- Full pipeline generating a single HTML artifact per unit combining multiple content sources (assignment text, explanations, code, compiler output, runtime output, question, references) with inline styling only.

## 2. Architecture

### 2.1 Implemented Components

1. `DiscussionPostFormatter` (Main)
   - Loads config
   - Prompts for unit/theme
   - Persists changes if modified
   - Prints raw and resolved configs
   - Exposes derived content variables

2. `Config`
   - Parses `config.txt` retaining line structure
   - Ordered line model (ENTRY / COMMENT / BLANK)
   - Updates existing keys in place; appends new keys
   - Resolves `<UNIT_NUMBER>` placeholders at access time
   - Loads file content for qualifying address keys (excludes `output_file_address`)
   - Malformed lines (no `=`) treated as comments
   - (Theme placeholder intentionally not implemented)

### 2.2 Planned Components (Not Yet Implemented)

- `ThemeLoader`: Load theme definition (JSON or properties) mapping token → inline style fragment.
- `Highlighter`: Tokenize Java (keywords, strings, comments, numbers, chars) and wrap spans with inline style.
- `InlineCodeProcessor`: Scan narrative text for inline code markers and wrap.
- `ContentBlock`: (type, content, metadata) for modular assembly.
- `HtmlAssembler`: Convert ordered `ContentBlock` list into final HTML snippet (single container).
- `ExecutionService`: Compile + run Java sources; capture diagnostics & runtime output.
- `FileCollector`: Enumerate code files for unit (secure traversal; no shell wildcards).
- `HtmlTidyIntegration`: Pretty-print / normalize HTML output.
- `Logger` abstraction (lightweight).

Grouping:
- Rendering: ThemeLoader, Highlighter, InlineCodeProcessor, HtmlAssembler
- Execution: FileCollector, ExecutionService
- Post-processing: HtmlTidyIntegration
- Support: Logger

## 3. Configuration Format

CURRENT EXAMPLE (values illustrative):
```
# Comments preserved
unit = 3
theme = default
output_file_address = ../assignments/unit_<UNIT_NUMBER>_discussion_post.html
assignment_text_file_address = ../assignments/assignment_text.txt
```

RULES:
- Lines beginning with `#` or `//` preserved verbatim.
- Only `<UNIT_NUMBER>` resolved at runtime (never rewritten into file).
- Malformed lines (lacking `=`) treated as comments (no data loss).
- First `=` splits key and value; additional `=` retained in value.
- Keys: `snake_case`; values are trimmed of leading/trailing spaces (inner spaces preserved).
- Derived content: Any key containing `address` (and ending `_file_address` by convention) except `output_file_address` triggers file read (subject to future error handling improvements).
- Derived variable naming: `snake_case_file_address` → `camelCaseFileContents`.

FUTURE:
- Additional keys: `main_class`, `theme_file`, `explanation_n_file`, `reference_file`, etc.
- Placeholder expansion API (explicit whitelist).
- Potential additional placeholder `<THEME>` (backlog).

## 4. Processing Workflow (Stages)

| Stage | Description | Milestone (Project Specs) |
|-------|-------------|---------------------------|
| 0 | Config editing + placeholder resolution | Pre-M1 (DONE) |
| 1 | Minimal HTML output (static template) | M1 |
| 2 | Embed text blocks & file existence fallbacks | M2 |
| 3 | Compilation + runtime capture | M3 |
| 4 | Syntax highlighting | M4 |
| 5 | Theme style application | M5 |
| 6 | ContentBlock architecture & HtmlAssembler refactor | M6 |
| 7 | Inline code + user overrides | M7 |
| 8 | Tidy / formatting pass | M9 |
| 9 | Test suite expansion & robustness | M10 |

## 5. Theming

CURRENT:
- `theme` scalar stored only.

PLANNED:
- Theme definition file: `themes/<name>.json` or `.properties`
  Example JSON (planned):
  ```json
  {
    "name": "default",
    "styles": {
      "keyword": "color:#0000ff;font-weight:bold;",
      "string": "color:#008000;",
      "comment": "color:#808080;font-style:italic;"
    }
  }
  ```
- Highlighter emits token classes; ThemeLoader maps to inline styles.
- Fallback: Any missing token style falls back to default theme’s token.

## 6. Syntax Highlighting (Planned MVP)

Regex pass order (to avoid nested conflicts):
1. Multi-line comments
2. Single-line comments
3. String literals (handle escapes)
4. Char literals
5. Keywords (word boundary)
6. Numbers
7. (Optional later) Annotations

Wrap tokens: `<span style="...">...</span>` using theme style fragments.

Non-goals (MVP):
- Unicode escape normalization
- Generic type coloring
- Annotation parameter parsing

## 7. HTML Assembly (Planned)

Container (tentative; `zoom` may be replaced with scalable font sizing):
```
<div style="padding:10px;width:600px;font-size:110%;border:0;">
  ... blocks ...
</div>
```

Block Types (planned):
- Assignment Text
- Sample Code (optional)
- Highlighted Source (per file)
- Compiler Messages
- Program Output
- Explanation(s)
- Question
- References

Each block:
- Heading (`<h3>` or `<h4>`)
- Content wrapper (`<div>` or `<pre><code>` as appropriate)
- Consistent margin (e.g. `margin-bottom:12px;`)

## 8. Error Handling Strategy (Target)

| Scenario | Action |
|----------|--------|
| Missing file | Insert placeholder notice + log warning |
| Compilation failure | Include compiler messages section; skip runtime |
| Runtime exception | Embed concise stack trace excerpt |
| Theme missing | Fallback to default; log |
| Invalid unit input | Retain previous value; notify user |
| Derived file read error | Insert placeholder (I/O error detail optional) |

CURRENT:
- Invalid unit input ignored (message output).
- Missing file may currently cause exception or empty content (robust handling pending Stage 2).

## 9. Testing Plan (Incremental)

Short-Term:
- Config round-trip (preserve comments, blank lines, ordering)
- Placeholder resolution test `<UNIT_NUMBER>`
- Derived content loading correctness (presence and naming)

Medium:
- Highlighter token coverage
- Execution compile/run scenarios (success/fail)
- HTML block ordering invariants

Long-Term:
- Snapshot/diff tests for assembled HTML stability
- Theme fallback behavior tests
- Performance sanity (< time thresholds)

Acceptance Example (Config Round-Trip):
Given an original config, after updating only `unit`, all other lines (including spacing & comments) remain byte-for-byte identical.

## 10. Performance Considerations

Expected scale: small (≤ 10 short Java files; text inputs < 50 KB each).  
- Config parse O(n) lines.  
- Highlighting O(n) characters per file (linear).  
No specialized optimization planned; profiling only if latency > target thresholds.

## 11. Security / Integrity

- Local-only execution; no network I/O.
- Execution confined to expected source directories.
- No arbitrary command invocation (compile/run limited to discovered sources).
- HTML output includes clear authorship note (planned).
- Inputs assumed trusted (future optional HTML escaping for untrusted sources).

## 12. Open Technical Decisions

| Topic | Decision Status | Notes |
|-------|-----------------|-------|
| Theme file format | JSON vs properties (TBD) | JSON favored for nested structure |
| Inline code markers | Backticks vs custom (TBD) | Leaning toward backticks for familiarity |
| HTML tidy approach | External lib vs simple formatter (TBD) | Evaluate after M1 |
| Placeholder generalization | Defer | Avoid token sprawl |
| main class detection | Explicit vs heuristic (TBD) | Likely explicit config key |
| Logging | Simple wrapper vs library (TBD) | Start simple; swap later if needed |

## 13. Change Log (Tech Spec)

0.1.2 Added derived file loading to CURRENT, stage/milestone table, refined component grouping, clarified error handling & naming.  
0.1.1 Adjusted to reflect minimal implementation; segregated planned components.  
0.1 Initial aspirational specification.

## 14. Next Engineering Tasks (Actionable)

1. Implement Stage 1 minimal HTML writer (M1).
2. Add existence checks + fallback placeholders (M2).
3. Introduce logging abstraction (info, warn, error).
4. Create skeleton Highlighter & ThemeLoader classes (interfaces only).
5. Add JUnit setup for Config & derived content tests.

## 15. Appendix

A. Minimal HTML Skeleton (planned Stage 1):
```
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<title>Unit <UNIT_NUMBER> Discussion Post</title>
</head>
<body>
<div style="padding:10px;width:600px;font-size:110%;border:0;">
  <h2>Discussion Post (Unit <UNIT_NUMBER>)</h2>
  <!-- Future blocks will be injected below in ordered sequence -->
</div>
</body>
</html>
```

B. Example Future Theme Reference Shortcut:
`keyword` → `span style="color:#0000ff;font-weight:bold;"`

C. Derived Key Example:
`assignment_text_file_address` → loads file → `assignmentTextFileContents` (String)

(End of specification)