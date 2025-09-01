# Discussion Post Formatter - Technical Specification (Updated)

Version: 0.1.1  
Date: 2025-09-01  
Repository: https://github.com/RuffusReeves/discussion_post_formatter  

## Reality vs Plan

This specification now separates CURRENT IMPLEMENTATION from PLANNED DESIGN to prevent overstatement.

## 1. Overview

CURRENT:
- Java console utility for editing and persisting unit and theme in config.txt with comment preservation.
- Resolves <UNIT_NUMBER> placeholder in values when displayed/used.

PLANNED:
- Full pipeline generating a single HTML artifact per unit combining multiple content sources (assignment text, explanations, code, compiler output, runtime output, question, references) with inline styling only.

## 2. Architecture

### 2.1 Implemented Components

1. DiscussionPostFormatter (Main)
   - Loads config
   - Prompts for unit/theme
   - Persists changes if modified
   - Prints raw and resolved configs

2. Config
   - Parses config.txt retaining line structure
   - Maintains ordered line model (ENTRY / COMMENT / BLANK)
   - Updates existing keys in place; appends new keys
   - Resolves <UNIT_NUMBER> placeholders at access time
   - (Theme placeholder intentionally not implemented)

### 2.2 Planned Components (Not Yet Implemented)

- ThemeLoader: Load theme definition (JSON or properties) mapping token → inline style fragment.
- Highlighter: Tokenize Java (keywords, strings, comments) and wrap spans with inline style attributes.
- InlineCodeProcessor: Scan narrative text for inline code markers and wrap appropriately.
- ContentBlock (record/class): (type, content, metadata) for modular assembly.
- HtmlAssembler: Convert ordered ContentBlock list into final HTML snippet with required container.
- ExecutionService: Compile + run Java sources, capturing compiler diagnostics and runtime output.
- FileCollector: Enumerate code files for unit (avoid shell wildcards; use secure traversal).
- HtmlTidyIntegration (optional): Pretty-print / normalize output.

## 3. Configuration Format

CURRENT:
```
# Comments preserved
unit = 3
theme = default
output_file_address = ../assignments/unit_<UNIT_NUMBER>_discussion_post.html
```

RULES:
- Lines beginning with # or // treated as comments and preserved verbatim.
- Only <UNIT_NUMBER> replaced when accessing values (never rewritten inside file).
- Malformed lines (lacking '=') are treated as comments to avoid data loss.

FUTURE:
- Possible addition: main_class, theme_file, explanation_n_file, reference_file, etc.
- Potential generic placeholder expansion API (whitelist tokens).

## 4. Processing Workflow (Staged)

Stage 0 (DONE): Config editing + placeholder resolution.  
Stage 1 (NEXT): Minimal HTML output using static template + resolved paths (no dynamic content).  
Stage 2: Ingest text files and embed content; simple <pre><code> blocks.  
Stage 3: Compilation and runtime capture integration.  
Stage 4: Syntax highlighting (regex-based MVP).  
Stage 5: ThemeLoader + inline style mapping.  
Stage 6: ContentBlock abstraction & HtmlAssembler refactor.  
Stage 7: Inline code detection in narrative text.  
Stage 8: Tidy / formatting pass.  
Stage 9: Test suite expansion & robustness improvements.

## 5. Theming

CURRENT:
- theme key stored only.

PLANNED:
- Theme definition file: themes/<name>.json or .properties
  Example JSON (planned):
  ```
  {
    "name": "default",
    "styles": {
      "keyword": "color:#0000ff;font-weight:bold;",
      "string": "color:#008000;",
      "comment": "color:#808080;font-style:italic;"
    }
  }
  ```
- Highlighter consumes resolved style fragments.

## 6. Syntax Highlighting (Planned MVP)

Regex pass order (provisional):
1. Multi-line comments
2. Single-line comments
3. String literals (handle escapes)
4. Char literals
5. Keywords boundary-matched
6. Numbers
Wrap each token in <span style="...">...</span> using theme style lookups.

## 7. HTML Assembly (Planned)

Container:
```
<div style="padding:10px;border:0;zoom:110%;width:600px;">
  ... blocks ...
</div>
```

Block Types (planned):
- Assignment Text
- Sample Code (optional)
- Highlighted Code (per source file)
- Compiler Messages
- Program Output
- Explanation(s)
- Question (Assignment Text segment + user question)
- References

Each block: consistent outer margin, heading, content area.

## 8. Error Handling Strategy (Target)

| Scenario | Action |
|----------|--------|
| Missing file | Insert placeholder notice + log |
| Compilation failure | Embed compiler output; skip runtime |
| Runtime exception | Embed stack trace or concise message |
| Theme missing | Fallback to default theme; note fallback |
| Invalid unit input | Retain previous value; notify user |

CURRENT: Only invalid unit (non-digit) silently ignored with message.

## 9. Testing Plan (Incremental)

Short-Term:
- Config round-trip (preserve comments, order)
- Placeholder resolution test

Medium:
- Highlighter token coverage
- Execution service compile/run scenarios
- HTML block ordering invariant tests

Long-Term:
- Snapshot/diff tests for HTML output stability
- Theme fallback behavior tests

## 10. Performance Considerations

Expected scale is small (a handful of short Java files).  
No optimization needed beyond O(n) line traversal for config and single-pass highlighting.

## 11. Security / Integrity

- Local-only execution (no network I/O planned).
- Avoid executing arbitrary external code outside controlled unit directories.
- Provide clear authorship footer to discourage unauthorized reuse.

## 12. Open Technical Decisions

| Topic | Decision Status |
|-------|-----------------|
| Theme file format | JSON vs properties (TBD) |
| Inline code markers | Backticks vs custom delimiters (TBD) |
| HTML tidy approach | External library vs simple formatter (TBD) |
| Placeholder generalization | Keep explicit (<UNIT_NUMBER>) until need arises |

## 13. Change Log (Tech Spec)
0.1.1 Adjusted to reflect actual minimal implementation; segregated planned components.  
0.1 Initial aspirational specification.

## 14. Next Engineering Tasks (Actionable)
1. Implement Stage 1 minimal HTML writer.
2. Add existence checks for key configured paths.
3. Introduce basic logging abstraction (or light wrapper to stdout).
4. Create skeleton Highlighter & ThemeLoader classes (interfaces only).
5. Add JUnit setup for Config tests.

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
<div style="padding:10px;border:0;zoom:110%;width:600px;">
  <h2>Discussion Post (Unit <UNIT_NUMBER>)</h2>
  <!-- Future blocks will be injected here -->
</div>
</body>
</html>
```

B. Example Future Theme Reference Shortcut:
keyword → span style="color:#0000ff;font-weight:bold;"

(End of specification)