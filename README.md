# Discussion Post Formatter

A Java application under development to generate formatted HTML discussion posts for course assignments.  
Current implemented functionality is deliberately minimal: safe, comment‑preserving configuration editing (unit and theme) plus placeholder resolution for <UNIT_NUMBER>. The full formatting / compilation pipeline described in earlier drafts is not yet present.

## Status (2025-09-01)

Implemented:
- Load config.txt preserving comments & blank lines
- Interactive prompt to update unit (digits) & theme (string)
- Persist changes back to config.txt (only changed keys rewritten)
- Resolve <UNIT_NUMBER> in paths at runtime (not written back)

Not Yet Implemented (Planned):
- Reading assignment / explanation / question / reference text files
- Source file enumeration and compilation / execution
- Syntax highlighting & theme file loading
- HTML block construction & tidy formatting
- Inline code detection
- Multi-section artifact generation

## Quick Start (Current Functionality)

Prerequisites:
- Java 17+ (recommended)  
- Git (if cloning)

Run (example if using plain javac):
```bash
javac -d out $(find src/main/java -name "*.java")
java -cp out formatter.DiscussionPostFormatter
```

Follow Prompts:
1. Enter a new unit number or press Enter to keep existing.
2. Enter a theme name or press Enter to keep existing.
3. The tool rewrites config.txt preserving comments and shows resolved values.

## Configuration (Current)

Example excerpt of `config.txt` (simplified):
```
# Core Settings
unit = 3
theme = default
output_file_address = ../assignments/unit_<UNIT_NUMBER>_discussion_post.html
```
Rules:
- Comments (# or //) and blank lines are preserved.
- Only <UNIT_NUMBER> is resolved at runtime; no theme substitution yet.
- Adding a new key appends it (original order retained).

## Roadmap (Next Steps)

Immediate (MVP Build Path):
1. Minimal HTML generator using existing configured file paths (even if files are placeholders).
2. File ingestion & fallback messaging when missing.
3. Introduce ContentBlock abstraction and HtmlAssembler.
4. Basic Java code highlighting (regex approach) with inline styles.

Later:
- Theme loader (JSON or properties-based)
- Compilation + runtime capture
- Inline code snippet parsing inside explanation text
- Question and references block integration
- HTML tidy / pretty print

## Development Notes

Directory Structure (current minimal):
```
src/main/java/formatter/
  Config.java
  DiscussionPostFormatter.java
config.txt
```

Classes Mentioned in Earlier Docs (Not Yet Added):
- Highlighter
- HtmlBuilder / HtmlAssembler
- ThemeLoader
- ContentBlock models
These remain planned abstractions.

## Contributing (Single-Developer Mode)

While in early development:
- Keep commits focused (config, formatting, future pipeline).
- Update specs alongside new functionality to avoid drift.
- Use feature branches only when introducing substantial new modules.

Suggested Commit Types:
- feat: for new functional capabilities (e.g., HTML output introduction)
- refactor: internal structure changes without new behavior
- chore: build script or documentation upkeep
- fix: defect corrections

## Testing (Planned)

Initial tests will focus on:
- Config parsing + comment preservation
- Placeholder resolution correctness
- Safe updating of only modified keys
Future:
- Highlighter token coverage
- Compilation/execution harness
- HTML assembly diff stability

## Academic Integrity

This tool formats and aggregates content; it must not fabricate assignment answers. Generated artifacts should retain clear authorship and references.

## License

Intended: MIT (to be added as LICENSE file once core formatter is in place).

## FAQ (Early Phase)

Q: Why are the specs mentioning features that don't exist yet?  
A: They define the planned architecture. This README now distinguishes implemented vs planned to reduce confusion.

Q: Can I already get HTML output?  
A: Not yet. The next milestone adds a minimal HTML writer.

Q: Will <THEME> placeholders work in paths?  
A: Not currently. Only <UNIT_NUMBER> is implemented.

---

(README aligned with repository’s current actual state. Update as milestones are completed.)