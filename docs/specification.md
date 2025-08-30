# Discussion Post Formatter - Technical Specification

## Overview

The Discussion Post Formatter is a Java application designed to automatically generate formatted HTML discussion posts from course assignments and code files. It combines assignment text, syntax-highlighted code, program execution output, and provides a structured template for student explanations.

## Architecture

### Core Components

#### 1. Main Entry Point (`DiscussionPostFormatter`)
- **Purpose**: Main application entry point and workflow orchestration
- **Responsibilities**:
  - Parse command-line arguments and configuration
  - Coordinate processing pipeline
  - Handle user interaction for unit number and theme selection
  - Output final HTML result

#### 2. Configuration System (`Config`)
- **Purpose**: Manage application configuration and file path resolution
- **Responsibilities**:
  - Load configuration from `config.txt`
  - Support placeholder replacement (e.g., `<UNIT_NUMBER>`)
  - Provide default values for missing configuration

#### 3. Content Processing Pipeline

##### a. InlineCodeProcessor (TODO)
- **Purpose**: Process inline code elements within assignment text
- **Responsibilities**:
  - Identify inline code patterns (e.g., `code` or \`\`\`code\`\`\`)
  - Apply basic syntax highlighting for inline snippets
  - Handle code escaping and HTML entity encoding

##### b. ThemeLoader
- **Purpose**: Load and manage syntax highlighting themes
- **Responsibilities**:
  - Load theme definitions from JSON files in `themes/` directory
  - Provide color and style mappings for syntax elements
  - Support fallback to default theme

##### c. Syntax Highlighter (`Highlighter`)
- **Purpose**: Apply syntax highlighting to Java code
- **Responsibilities**:
  - Tokenize Java source code
  - Apply theme-based coloring to keywords, strings, comments, etc.
  - Generate HTML with embedded CSS styles

#### 4. Content Assembly

##### a. ContentBlock (Interface/Record)
- **Purpose**: Represent different types of content blocks
- **Properties**:
  - `type`: Enum (ASSIGNMENT_TEXT, HIGHLIGHTED_CODE, PROGRAM_OUTPUT, EXPLANATION_PLACEHOLDER)
  - `content`: Raw content string
  - `metadata`: Optional metadata map

##### b. HtmlAssembler
- **Purpose**: Assemble final HTML output from content blocks
- **Responsibilities**:
  - Take list of ContentBlock objects
  - Apply consistent HTML structure and styling
  - Generate semantic HTML with proper CSS classes

#### 5. Utility Services

##### a. File I/O (`Utils`)
- **Purpose**: Handle file operations
- **Responsibilities**:
  - Read assignment and code files
  - Write HTML output
  - Execute Java programs and capture output

## Configuration Format

The `config.txt` file uses Java Properties format:

```properties
# File paths with placeholder support
assignment_file=path/to/unit_<UNIT_NUMBER>_assignment.txt
code_file=path/to/unit_<UNIT_NUMBER>/Begin.java
output_file=path/to/unit_<UNIT_NUMBER>_discussion_post.html

# Appearance settings
theme=default
```

## Theme System

Themes are defined in JSON format in the `themes/` directory:

```json
{
  "name": "theme_name",
  "description": "Theme description",
  "background": "#ffffff",
  "foreground": "#000000",
  "colors": {
    "keyword": {"color": "#0000ff", "fontWeight": "bold"},
    "string": {"color": "#008000"},
    "comment": {"color": "#808080", "fontStyle": "italic"}
  }
}
```

## Processing Workflow

1. **Initialization**
   - Load configuration from `config.txt`
   - Prompt user for unit number and optional theme override
   - Initialize theme system

2. **Content Loading**
   - Read assignment text file
   - Read Java source code file
   - Attempt to compile and execute Java code to capture output

3. **Content Processing**
   - Process inline code elements in assignment text
   - Apply syntax highlighting to Java source code
   - Create ContentBlock objects for each content type

4. **HTML Assembly**
   - Assemble ContentBlock objects into structured HTML
   - Apply consistent styling and layout
   - Include placeholder for student explanation

5. **Output**
   - Write final HTML to configured output file
   - Display success message with file location

## Future Enhancements

### Phase 2: Enhanced Processing
- [ ] Support for multiple programming languages
- [ ] Advanced inline code processing with language detection
- [ ] Markdown support in assignment text
- [ ] Interactive theme customization

### Phase 3: Integration & Automation
- [ ] Integration with IDE plugins
- [ ] Batch processing of multiple units
- [ ] Git integration for version tracking
- [ ] Template customization system

### Phase 4: Advanced Features
- [ ] Web-based interface
- [ ] Real-time preview
- [ ] Collaborative editing
- [ ] Export to multiple formats (PDF, DOCX)

## Error Handling Strategy

- **Configuration Errors**: Provide clear error messages with suggested fixes
- **File Not Found**: Graceful degradation with placeholder content
- **Compilation Errors**: Include compilation output in HTML for debugging
- **Theme Loading Errors**: Fall back to built-in default theme

## Testing Strategy

- **Unit Tests**: Test each component in isolation
- **Integration Tests**: Test complete workflow with sample data
- **Theme Tests**: Validate theme loading and application
- **Configuration Tests**: Test various configuration scenarios

## Dependencies

### Current
- Java 11+ standard library
- No external dependencies (by design for simplicity)

### Future Considerations
- **JSON Processing**: Jackson or minimal JSON parser
- **HTML Tidying**: JSoup for robust HTML generation
- **Testing**: JUnit 5 for comprehensive test coverage