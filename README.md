# Discussion Post Formatter

A Java utility to generate a self‑contained HTML “discussion post” artifact for course assignments.  
It ingests multiple textual inputs (assignment, explanations, questions, references), highlights Java code,
optionally compiles & runs the assignment source, and assembles everything into a single styled HTML document
(with all styling inlined—no external CSS).

## Status (2025-09-03)

### Implemented
- Comment & blank line–preserving configuration (`config.txt`)
- Interactive updating of `unit` (digits) and `theme` (string)
- `<UNIT_NUMBER>` placeholder resolution at runtime (not persisted back into the file)
- Automatic derived file content loading: any key containing `address` (except `output_file_address`)
  is loaded into a non-persisted camelCase `...Contents` key  
  (e.g. `assignment_text_file_address` → `assignmentTextFileContents`)
- HTML generation for the discussion post (sections: intro, explanations, question, code, compiler messages, outputs, references)
- Syntax highlighting of Java code via built-in `Highlighter` (theme name taken from config)
- Inline code processing (backticks, triple backticks, `<code>` tags) via `InlineCodeProcessor`
- Optional compilation & execution of the assignment Java source (stdout captured; minimal stderr handling)
- Inclusion of both previously captured output file contents (if present) and fresh execution output
- Inline-only styling (no external CSS or `<style>` blocks)
- Basic theme JSON loading (`ThemeLoader`) for future styling integration (currently informational)

### New Since Previous Update (2025-09-01 → 2025-09-03)
- Added `Highlighter` for inline-style syntax coloring
- Added `InlineCodeProcessor` for prose code formatting
- Added HTML assembly in `DiscussionPostFormatter` with structured sections
- Integrated optional compile/run step (`Utils.runJavaFile`)
- Added execution + highlighting workflow and output file writing
- Added helper HTML escaping & standardized section rendering

### Not Yet Implemented (Planned)
- CLI / flags for non-interactive mode (skip prompts, toggle execution/highlighting)
- Structured capture & display of compilation errors (stderr panel)
- Separate HTML builder / templating abstraction (current logic is monolithic)
- Test suite (unit + snapshot/diff tests for HTML & tokenization)
- Multiple placeholder types (e.g. `<THEME>`)
- Alternate language highlighting (presently Java-only heuristic)
- Diff / patch output mode for incremental previews
- Performance optimization for very large source files
- Validation & graceful degradation when required input files are missing
- Optional markdown-to-HTML conversion for prose (currently inline code only)

## Quick Start

Prerequisites:
- Java 17+
- (Optional) A `themes/` directory with JSON theme descriptors (currently partially utilized)
- `config.txt` at project root (see example below)
- Input text files referenced by the `*_address` keys

Compile & Run (Unix-like example):
```bash
# Compile
find src/main/java -name "*.java" > sources.list
javac -d out @sources.list

# Run
java -cp out formatter.DiscussionPostFormatter