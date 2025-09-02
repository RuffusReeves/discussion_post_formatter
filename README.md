# Discussion Post Formatter

A Java application under early development to generate formatted HTML discussion posts for course assignments.  
Current implemented functionality is deliberately minimal: safe, comment‑preserving configuration editing (unit and theme) plus runtime placeholder resolution for `<UNIT_NUMBER>` and automatic loading of input file contents referenced by `*_file_address` keys (except the output path). The full formatting / compilation / highlighting pipeline is not yet implemented.

## Status (2025-09-02)

Implemented:
- Load `config.txt` preserving comments & blank lines
- Interactive prompt to update `unit` (digits) & `theme` (string)
- Persist only changed keys back to `config.txt` (original order & comments preserved)
- Resolve `<UNIT_NUMBER>` in paths at runtime (never written back into file)
- Automatic derived file content loading: config keys containing `address` (excluding `output_file_address`) are automatically loaded and exposed as derived, non‑persisted variables with camelCase names (e.g. `assignment_text_file_address` → `assignmentTextFileContents`)
- Graceful ignore of malformed lines (treated as comments)
- (NEW in docs) Documentation alignment to avoid overstating features

Not Yet Implemented (Planned):
- Source file enumeration and compilation / execution
- Syntax highlighting & theme file loading
- HTML block construction & tidy formatting
- Inline code detection
- Multi-section artifact generation
- Unit source execution output capture
- Theming beyond stored scalar value

Note: Reading assignment / explanation / question / references text files is supported via automatic file content loading when their `*_file_address` keys are provided in config (except the output file).

## Quick Start (Current Functionality)

Prerequisites:
- Java 17+ (recommended)  
- Git (if cloning)

Unix-like shell example:
```bash
javac -d out $(find src/main/java -name "*.java")
java -cp out formatter.DiscussionPostFormatter
```

Windows (PowerShell) example:
```powershell
New-Item -ItemType Directory -Force -Path out | Out-Null
Get-ChildItem -Recurse src/main/java -Filter *.java | ForEach-Object { $_.FullName } | `
  & javac -d out -classpath out @-
java -cp out formatter.DiscussionPostFormatter
```
(Or simply open in an IDE and run `formatter.DiscussionPostFormatter`.)

Follow Prompts:
1. Enter a new unit number or press Enter to keep existing.
2. Enter a theme name or press Enter to keep existing.
3. The tool rewrites `config.txt` (only changed lines) and displays raw vs resolved values.

## Configuration (Current)

Example excerpt (values illustrative; may differ from your local `config.txt`):
```
# Core Settings
unit = 3
theme = default

# Input file addresses (automatically loaded as derived contents)
assignment_text_file_address = ../assignments/assignment_text.txt
code_file_address = ../assignments/code_sample.java

# Final Output (excluded from automatic loading by design)
output_file_address = ../assignments/unit_<UNIT_NUMBER>_discussion_post.html
```

Automatic File Content Loading:
- Keys containing `address` (except `output_file_address`) automatically load their file contents at runtime.
- Creates derived camelCase keys: `assignment_text_file_address` → `assignmentTextFileContents`.
- Derived values are not persisted to `config.txt` but are accessible during the session.
- `output_file_address` is excluded (treated as output, not input).
- If a file is missing, current behavior: may raise an exception or show empty content (robust fallback planned).

Placeholder Behavior:
- Only `<UNIT_NUMBER>` is resolved at runtime in file paths.
- Placeholders are resolved before attempting to load file contents.
- Derived content keys do not contain placeholders (resolution happens first).
- Other tokens (e.g. `<THEME>`) are currently treated as literal text.

Rules:
- Comments (`#` or `//`) and blank lines are preserved.
- Only `<UNIT_NUMBER>` is resolved at runtime; no theme substitution yet.
- Adding a new key appends it while retaining overall ordering.
- Malformed lines without `=` are treated as comments (not discarded).

## Roadmap (Next Steps)

Immediate (MVP Path):
1. Minimal HTML generator using existing configured file paths (M1 / Stage 1).
2. File existence checks & fallback messaging (M2 aspects).
3. Introduce `ContentBlock` abstraction and `HtmlAssembler`.
4. Basic regex-based Java syntax highlighting (keywords, strings, comments).

Later:
- Theme loader (JSON or properties-based)
- Compilation + runtime capture
- Inline code snippet parsing inside explanation text
- Question and references block integration
- HTML tidy / pretty print
- CLI flags for non-interactive usage
- Additional placeholders (only if justified)

## Development Notes

Directory Structure (current minimal):
```
src/main/java/formatter/
  Config.java
  DiscussionPostFormatter.java
config.txt
```

Planned / Not Yet Added Classes:
- Highlighter
- HtmlAssembler
- ThemeLoader
- ContentBlock models
- ExecutionService
- FileCollector

## Contributing (Single-Developer Mode)

While in early development:
- Keep commits focused (config, formatting groundwork, future pipeline).
- Update specs alongside new functionality to avoid drift.
- Use feature branches for substantial new modules.

Suggested Commit Types:
- feat: new functional capability
- refactor: internal structure change without new behavior
- chore: build script / documentation / housekeeping
- fix: defect correction
- docs: documentation-only changes
- test: test-only additions or changes

## Testing (Planned)

Initial tests will focus on:
- Config parsing + comment preservation
- Placeholder resolution correctness
- Safe updating of only modified keys

Future:
- Highlighter token coverage
- Compilation / execution harness
- HTML assembly diff stability

## Academic Integrity

This tool formats and aggregates content; it must not fabricate assignment answers. Generated artifacts should retain clear authorship and references.

## License

Intended: MIT (LICENSE file to be added before first release that produces HTML output).

## FAQ (Early Phase)

Q: Why do specs mention features that don't exist yet?  
A: Specs define the planned architecture. Status labels distinguish implemented vs planned.

Q: Can I already get HTML output?  
A: Not yet. The next milestone introduces a minimal HTML writer.

Q: Will `<THEME>` placeholders work in paths?  
A: Not currently. Only `<UNIT_NUMBER>` is implemented; additional tokens are backlog items.

Q: What about the misspelling `dicussion_question.txt` I saw earlier?  
A: It will be corrected to `discussion_question.txt`; update your config or rely on a transitional compatibility alias if added.

---

(README aligned with repository’s current actual state. Update as milestones are completed.)