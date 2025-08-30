# Discussion Post Formatter

A Java application that automatically generates formatted HTML discussion posts from course assignments and code files, featuring syntax highlighting and structured output.

## Quick Start

### Prerequisites
- Java 11 or higher
- Git (for development)

### Running the Application

1. **Clone the repository**:
   ```bash
   git clone https://github.com/RuffusReeves/discussion_post_formatter.git
   cd discussion_post_formatter
   ```

2. **Build and run**:
   ```bash
   ./gradlew run
   ```

3. **Follow the prompts**:
   - Enter your unit number when prompted
   - Optionally select a theme (or press Enter for default)
   - The application will generate an HTML file based on your configuration

### Sample Configuration

Edit `config.txt` to point to your files:

```properties
assignment_file=path/to/unit_<UNIT_NUMBER>_assignment.txt
code_file=path/to/unit_<UNIT_NUMBER>/Begin.java
output_file=path/to/unit_<UNIT_NUMBER>_discussion_post.html
theme=default
```

The `<UNIT_NUMBER>` placeholder will be replaced with the unit number you enter.

## Development Guide

### Project Structure

```
discussion_post_formatter/
├── src/main/java/formatter/           # Main source code
│   ├── DiscussionPostFormatter.java  # Main entry point
│   ├── Config.java                   # Configuration management
│   ├── Highlighter.java             # Syntax highlighting
│   ├── HtmlBuilder.java             # HTML generation
│   ├── Utils.java                   # File I/O utilities
│   └── [skeletons for future classes]
├── themes/                           # Syntax highlighting themes
│   └── default.json                 # Default theme definition
├── docs/                            # Documentation
│   └── specification.md             # Technical specification
├── config.txt                       # Sample configuration
├── build.gradle                     # Build configuration
└── README.md                        # This file
```

### Building from Source

```bash
# Compile the project
./gradlew build

# Run tests (when available)
./gradlew test

# Create distribution
./gradlew distZip
```

### Branching Strategy

We follow a simplified Git workflow suitable for individual development:

#### Main Branches
- **`main`**: Stable, working code
- **`develop`**: Integration branch for new features

#### Feature Development
1. **Create a feature branch** from `develop`:
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b feature/your-feature-name
   ```

2. **Work on your feature**:
   ```bash
   # Make changes
   git add .
   git commit -m "Add feature description"
   ```

3. **Push and create PR**:
   ```bash
   git push origin feature/your-feature-name
   # Create pull request to merge into develop
   ```

4. **After PR approval**, merge to develop and eventually to main

#### Hotfixes
For urgent fixes to main:
```bash
git checkout main
git checkout -b hotfix/issue-description
# Make fix
git push origin hotfix/issue-description
# Create PR to main
```

## Available Themes

The application includes several built-in themes:
- `default` (light background, dark text)
- `tango` (classic GNOME theme colors)
- `monokai` (dark theme inspired by Sublime Text)
- `rrt` (Visual Studio dark theme)
- `light` (minimal light theme)
- `dark` (minimal dark theme)

### Adding Custom Themes

1. Create a new JSON file in the `themes/` directory
2. Follow the format in `themes/default.json`
3. Use the theme name (filename without .json) when running the application

## Development Backlog

### Current Phase: Foundation (v0.1)
- [x] Basic project structure and build system
- [x] Core configuration system with placeholder support
- [x] Basic syntax highlighting for Java
- [x] HTML generation with theming
- [x] File I/O and program execution utilities
- [ ] Enhanced error handling and validation
- [ ] Comprehensive documentation

### Next Phase: Enhanced Processing (v0.2)
- [ ] Implement InlineCodeProcessor for assignment text
- [ ] Add ContentBlock architecture for modular content
- [ ] Implement ThemeLoader for JSON-based themes
- [ ] Add HtmlAssembler for better HTML structure
- [ ] Support for additional programming languages
- [ ] Markdown processing for assignment text

### Future Phases
- [ ] Unit testing framework and comprehensive tests
- [ ] Integration with popular IDEs
- [ ] Web-based interface for easier use
- [ ] Batch processing capabilities
- [ ] Export to additional formats (PDF, DOCX)
- [ ] Template customization system

## Contributing

### Getting Started
1. Fork the repository
2. Follow the branching strategy outlined above
3. Read `docs/specification.md` for architectural guidance
4. Look for `TODO` comments in the code for immediate tasks

### Code Style
- Follow standard Java conventions
- Add JavaDoc comments for public methods
- Include `TODO` comments for incomplete implementations
- Keep classes focused on single responsibilities

### Before Submitting PRs
1. Ensure code compiles without warnings
2. Test your changes manually
3. Update documentation if needed
4. Add TODO comments for future enhancements

## Troubleshooting

### Common Issues

**"Could not run program"**: 
- Ensure Java files are in the correct location
- Check that file paths in config.txt are correct
- Verify Java source code compiles without errors

**"Theme not found"**:
- Check theme name spelling
- Ensure theme file exists in `themes/` directory
- Falls back to default theme automatically

**Configuration errors**:
- Verify `config.txt` format (Java Properties)
- Check file paths exist and are accessible
- Ensure `<UNIT_NUMBER>` placeholder is used correctly

### Getting Help
- Check the `docs/specification.md` for detailed architecture
- Look for `TODO` comments in code for implementation status
- Create an issue for bugs or feature requests

## License

This project is intended for educational use. Please respect course policies regarding code sharing and collaboration.