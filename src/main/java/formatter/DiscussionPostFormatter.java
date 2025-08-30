package formatter;

import java.util.Arrays;
import java.util.Scanner;

/**
 * Main entry point for the Discussion Post Formatter application.
 * 
 * This class orchestrates the complete workflow for generating formatted HTML
 * discussion posts from course assignments and code files. It demonstrates
 * the processing pipeline and provides a minimal runnable example.
 * 
 * TODO: Refactor into smaller, testable methods
 * TODO: Add command-line argument parsing for batch mode
 * TODO: Implement comprehensive error handling with user-friendly messages
 * TODO: Add logging framework for debugging and monitoring
 */
public class DiscussionPostFormatter {
    
    /**
     * Main application entry point.
     * 
     * Current workflow:
     * 1. Interactive user input for unit number and theme
     * 2. Configuration loading with placeholder replacement
     * 3. Content file loading (assignment, code)
     * 4. Code execution and output capture
     * 5. Syntax highlighting application
     * 6. HTML assembly and output
     * 
     * TODO: Split this into smaller, focused methods
     * TODO: Add validation for user inputs
     * TODO: Implement retry logic for file operations
     */
    public static void main(String[] args) throws Exception {
        System.out.println("Discussion Post Formatter skeleton loaded.");
        
        Scanner scanner = new Scanner(System.in);

        // 1) Ask unit number
        System.out.print("Enter the unit number: ");
        String unitNumber = scanner.nextLine().trim();

        // 2) Load config with placeholder replacement
        Config config = Config.load("config.txt", unitNumber);
        System.out.println("Unit=" + unitNumber + ", Theme=" + config.theme);

        // 3) Optional theme override
        String[] themes = Highlighter.availableThemes();
        Arrays.sort(themes);
        System.out.print("Enter theme (" + String.join(", ", themes) + ") or press Enter to use '" + config.theme + "': ");
        String themeInput = scanner.nextLine().trim();
        if (!themeInput.isEmpty()) {
            config.theme = themeInput;
        }

        scanner.close();

        // TODO: Implement ThemeLoader.loadTheme() to load from themes/default.json
        // TODO: Implement InlineCodeProcessor.process() for assignment text
        // TODO: Implement ContentBlock architecture for modular content handling
        // TODO: Implement HtmlAssembler.assemble() for better HTML structure

        // 4) Read assignment & code
        String assignmentText = Utils.readFile(config.assignmentFile);
        String codeText = Utils.readFile(config.codeFile);

        // 5) (Optional) run the Java file to capture output
        String programOutput;
        try {
            programOutput = Utils.runJavaFile(config.codeFile);
        } catch (Exception e) {
            programOutput = "(Could not run program: " + e.getMessage() + ")";
        }

        // 6) Highlight code
        String highlightedCode = Highlighter.highlight(codeText, config.theme);

        // 7) Build HTML
        String html = HtmlBuilder.build(assignmentText, highlightedCode, programOutput);

        // 8) Write output
        Utils.writeFile(config.outputFile, html);

        System.out.println("Discussion post generated at: " + config.outputFile);
    }
}
