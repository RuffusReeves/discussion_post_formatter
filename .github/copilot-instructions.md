
# Discussion Post Formatter

Discussion Post Formatter is a Java 17+ console application for generating formatted HTML discussion posts from course assignments. Currently implements configuration management with comment preservation and placeholder resolution. The full HTML generation pipeline is planned but not yet implemented.

Always reference these instructions first and fallback to search or bash commands only when you encounter unexpected information that does not match the info here.

## Working Effectively

### Bootstrap and Build
- Ensure Java 17+ is available: `java -version` (must show 17+)
- Build with Gradle: `./gradlew build` -- takes 1-2 seconds. Set timeout to 30+ seconds.
- Alternative build: `javac -d out $(find src/main/java -name "*.java")` -- takes 1 second. Set timeout to 30+ seconds.
- Clean build: `./gradlew clean build` -- takes 1-2 seconds total. Set timeout to 30+ seconds.

### Run the Application
- With Gradle: `./gradlew run` -- starts interactive console app
- With javac: `java -cp out formatter.DiscussionPostFormatter` (after building with javac)
- IMPORTANT: Application requires user input and shows file warnings (this is normal behavior)
- Expected warnings about missing assignment files - these are expected in a fresh clone

### Test Commands
- `./gradlew test` -- runs instantly (no tests exist yet, this is normal)
- No linting tools configured - code style validation not available

## Validation

### Manual Testing Scenarios
ALWAYS manually test the application after making changes by running through this complete scenario:

1. **Build and run**: `./gradlew clean build && ./gradlew run`
2. **Test unit change**: Enter a new unit number (e.g., "5"), press Enter
3. **Test theme change**: Select a different theme by number or name (e.g., "2" for monokai)
4. **Verify output**: Check that:
   - Configuration shows "Configuration updated and saved to config.txt"
   - Raw config shows `<UNIT_NUMBER>` tokens unchanged
   - Resolved config shows tokens replaced with actual unit number
   - Theme loaded message appears
   - No error messages (file warnings are expected)

5. **Test persistence**: Run `./gradlew run` again and verify:
   - New unit and theme values are retained
   - Enter key to keep existing values works

### Expected Runtime Behavior
- File warnings about missing assignment files are NORMAL - ignore these
- Application completes in under 10 seconds for typical interactive session
- Configuration file (config.txt) preserves comments and blank lines when updated
- Placeholder resolution shows `<UNIT_NUMBER>` replaced with actual numbers

## Common Tasks

### Repository Structure
```
/
├── src/main/java/formatter/     # All Java source files (10 files)
│   ├── DiscussionPostFormatter.java  # Main entry point
│   ├── Config.java              # Configuration management
│   ├── Utils.java               # File operations
│   ├── HtmlAssembler.java       # HTML generation (planned)
│   ├── Highlighter.java         # Syntax highlighting (planned)
│   ├── Theme.java               # Theme data structures
│   ├── ThemeLoader.java         # Theme loading
│   ├── ContentBlock.java        # Content block interface
│   ├── HtmlBuilder.java         # HTML utilities
│   └── InlineCodeProcessor.java # Code processing
├── themes/                      # Theme JSON files (9 themes)
├── config.txt                   # Main configuration file
├── build.gradle                 # Gradle build configuration
└── gradlew                      # Gradle wrapper script
```

### Key Configuration Files

#### build.gradle
```gradle
plugins {
    id 'java'
    id 'application'
}

application {
    mainClass = 'formatter.DiscussionPostFormatter'
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
```

#### config.txt (example excerpt)
```
# Core Settings
unit = 3
theme = default
output_file_address = ../assignments/unit_<UNIT_NUMBER>_discussion_post.html
```

### Available Themes
- light, monokai, tango, rrt, default, solarized-light, dark, sunburst, solarized-dark
- Themes are JSON files in `themes/` directory defining syntax highlighting colors
- Theme selection is interactive via numbered menu or name entry

## Development Notes

### Current Implementation Status
- ✅ Configuration loading/saving with comment preservation
- ✅ Interactive unit and theme selection
- ✅ Placeholder resolution for `<UNIT_NUMBER>`
- ✅ Theme loading from JSON files
- ⏳ HTML generation pipeline (planned)
- ⏳ Java syntax highlighting (planned)
- ⏳ File content processing (planned)

### Project Architecture
- Main class: `formatter.DiscussionPostFormatter`
- Config management: Comment-preserving key-value parser in `Config.java`
- Modular design with separated concerns (themes, HTML generation, highlighting)
- Uses standard Java libraries only (no external dependencies)

### Making Changes
- Always build and test interactively after changes: `./gradlew clean build && ./gradlew run`
- Test both unit and theme modification scenarios
- Verify config.txt preservation of comments and structure
- Check that placeholder resolution works correctly
- No automated tests exist - rely on manual validation
- CRITICAL: Always run the full interactive validation scenario after any changes to core logic

### Common Development Commands
```bash
# Fresh build and test
./gradlew clean build && ./gradlew run

# Alternative build method
rm -rf out && javac -d out $(find src/main/java -name "*.java") && java -cp out formatter.DiscussionPostFormatter

# Check Java version
java -version

# View all Gradle tasks
./gradlew tasks

# Key Gradle tasks:
# ./gradlew run        -- Run the application
# ./gradlew build      -- Compile and package
# ./gradlew clean      -- Delete build artifacts
# ./gradlew test       -- Run tests (none exist yet)
```

### Troubleshooting
- **Build fails**: Ensure Java 17+ is installed and JAVA_HOME is set correctly
- **Application hangs**: Application requires interactive input - provide unit number and theme selection
- **File warnings**: Normal behavior for missing assignment files - application continues correctly
- **Configuration not saving**: Check file permissions on config.txt
- **Themes not loading**: Verify themes/ directory exists with JSON files

### Performance Expectations
- Build time: 1-2 seconds (very fast)
- Application startup: Under 5 seconds
- Interactive session: Under 10 seconds total
- Memory usage: Minimal (standard Java console app)

All operations in this repository are fast - no need for extended timeouts or build monitoring.
