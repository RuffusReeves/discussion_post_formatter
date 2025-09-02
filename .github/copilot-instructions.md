# Discussion Post Formatter - Development Instructions

Always reference these instructions first and fallback to search or bash commands only when you encounter unexpected information that does not match the info here.

## Working Effectively

### Bootstrap, Build, and Test the Repository
- Run all commands from the repository root directory
- Ensure Java 17+ is installed (`java --version` should show 17+)
- `./gradlew build` -- builds the project. Takes ~15 seconds on first run, <1 second subsequent runs. NEVER CANCEL. Set timeout to 30+ minutes.
- `./gradlew test` -- runs tests (currently no tests exist, completes in <2 seconds). NEVER CANCEL. Set timeout to 15+ minutes.
- `./gradlew clean` -- cleans build artifacts (<1 second)

### Alternative Build Method (Manual Compilation)
- `mkdir -p out` -- create output directory
- `javac -d out $(find src/main/java -name "*.java")` -- compile with javac (~1 second)
- `java -cp out formatter.DiscussionPostFormatter` -- run with manual classpath

### Run the Application
- **ALWAYS run the bootstrapping steps first** (`./gradlew build`)
- Primary method: `./gradlew run` -- starts interactive application (~1-2 seconds to start)
- Alternative: `java -cp out formatter.DiscussionPostFormatter` (after manual compilation)
- **Interactive Usage**: Application prompts for:
  1. New unit number (digits only, Enter to keep current)
  2. Theme selection from 9 available themes (light, monokai, tango, rrt, default, solarized-light, dark, sunburst, solarized-dark)
- **Expected Warnings**: Application will show warnings about missing file paths - this is normal for current implementation

### Validation
- **ALWAYS manually validate any new code by running the application end-to-end**
- **CRITICAL VALIDATION SCENARIO**: After making changes, run `./gradlew run` and test the complete workflow:
  1. Enter a unit number (e.g., "2")
  2. Select a theme (e.g., "dark" or number "7")
  3. Verify config.txt is updated correctly
  4. Verify application shows both raw and resolved configuration
  5. Verify theme loading confirmation message appears
- **Build Validation**: Always run `./gradlew build` after code changes to ensure compilation succeeds
- **Configuration Validation**: Check that config.txt preserves comments and updates only the changed values
- You can build and run the application successfully - it's fully functional for configuration editing
- **File Path Warnings**: The application shows warnings about missing files - this is expected behavior as the full file pipeline is not yet implemented

## Timing and Timeout Guidelines
- **NEVER CANCEL** any build or test commands
- Build (first run): 15 seconds, set timeout to 30+ minutes
- Build (subsequent): <1 second, set timeout to 10+ minutes  
- Test: <2 seconds, set timeout to 15+ minutes
- Clean: <1 second, set timeout to 5+ minutes
- Run: 1-2 seconds to start, interactive mode continues until user completes prompts

## Project Structure and Navigation

### Repository Root
```
.
├── README.md                    -- Main documentation
├── build.gradle                 -- Gradle build configuration  
├── config.txt                   -- Application configuration (editable)
├── src/main/java/formatter/     -- Source code directory
├── themes/                      -- Theme JSON files (9 themes)
├── docs/                        -- Technical specifications
├── gradlew                      -- Gradle wrapper (Unix)
├── gradlew.bat                  -- Gradle wrapper (Windows)
└── .github/                     -- GitHub configuration
```

### Source Code (`src/main/java/formatter/`)
- `DiscussionPostFormatter.java` -- Main application class with interactive prompts
- `Config.java` -- Configuration file handler with comment preservation
- `Utils.java` -- File I/O and Java compilation utilities
- `ThemeLoader.java` -- Theme JSON loading functionality
- `Theme.java` -- Theme data model
- `Highlighter.java` -- Syntax highlighting (planned feature)
- `HtmlAssembler.java` -- HTML generation (planned feature)
- `HtmlBuilder.java` -- HTML construction utilities (planned feature)
- `ContentBlock.java` -- Content abstraction (planned feature)
- `InlineCodeProcessor.java` -- Inline code processing (planned feature)

### Key Configuration Files
- `config.txt` -- Main application configuration with placeholder resolution (`<UNIT_NUMBER>`)
- `themes/*.json` -- Theme definitions for syntax highlighting (10 style tokens each)
- `build.gradle` -- Java 17, application plugin, main class: `formatter.DiscussionPostFormatter`

## Development Status and Limitations

### Currently Implemented (Fully Functional)
- Configuration editing with comment and blank line preservation
- Interactive unit number and theme selection
- Placeholder resolution for `<UNIT_NUMBER>` tokens
- Theme loading from JSON files
- Safe configuration persistence (only changed values rewritten)

### Not Yet Implemented (Planned Features)
- HTML generation pipeline
- File content ingestion from configured paths
- Java source compilation and execution
- Syntax highlighting application
- Full discussion post assembly

### Expected Behavior
- **File Path Warnings**: Application shows warnings for missing files referenced in config.txt - this is normal
- **Theme Loading**: Application successfully loads and validates selected themes
- **Config Updates**: Only modified configuration values are rewritten, preserving file structure

## Common Development Tasks

### Making Configuration Changes
- Edit `config.txt` directly for permanent changes, OR
- Run `./gradlew run` and use interactive prompts for unit/theme changes
- **Always verify**: Configuration preserves comments and updates only changed values

### Adding New Source Files
- Place in `src/main/java/formatter/` directory
- Follow existing package structure (`package formatter;`)
- Run `./gradlew build` to verify compilation
- Update main class imports if needed

### Working with Themes
- Theme files located in `themes/` directory (JSON format)
- 9 available themes: light, monokai, tango, rrt, default, solarized-light, dark, sunburst, solarized-dark
- Each theme contains 10 style token definitions
- Test theme loading via application: `./gradlew run` and select different theme

### Gradle Tasks Reference
- `./gradlew build` -- compile and assemble
- `./gradlew run` -- execute application
- `./gradlew clean` -- remove build artifacts  
- `./gradlew test` -- run test suite
- `./gradlew tasks` -- list all available tasks
- `./gradlew jar` -- create JAR file
- `./gradlew javadoc` -- generate documentation

### File Content from Common Commands

#### Repository Contents
```bash
ls -la
# Shows: README.md, build.gradle, config.txt, src/, themes/, docs/, gradlew*
```

#### Source Structure
```bash
find src -name "*.java"
# Shows: 10 Java files in src/main/java/formatter/
```

#### Available Themes
```bash
ls themes/
# Shows: dark.json, default.json, light.json, monokai.json, rrt.json, 
#        solarized-dark.json, solarized-light.json, sunburst.json, tango.json
```

## Troubleshooting

### Build Issues
- Ensure Java 17+ is installed: `java --version`
- Clean and rebuild: `./gradlew clean build`
- Check Gradle wrapper permissions: `chmod +x gradlew`

### Runtime Issues  
- Verify working directory is repository root
- Check config.txt exists and is readable
- Missing file warnings are expected behavior

### Common Gotchas
- **DO NOT** try to fix missing file path warnings - they are expected
- **DO NOT** cancel builds that appear to hang - wait for completion
- **ALWAYS** test interactively after making changes to user input handling
- **REMEMBER** that this is an early-stage project - full pipeline features are not yet implemented

## Validation Checklist for Changes
- [ ] `./gradlew clean build` completes successfully
- [ ] `./gradlew run` starts without errors
- [ ] Interactive prompts work correctly (unit number and theme selection)
- [ ] Config.txt is updated appropriately (preserving comments)  
- [ ] Both raw and resolved configurations display correctly
- [ ] Theme loading confirmation appears
- [ ] Application exits cleanly after showing resolved output

**CRITICAL**: Always run through the complete validation checklist after making any code changes.