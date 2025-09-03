package formatter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * DiscussionPostFormatter
 *
 * Responsibilities (updated):
 *  1. Load config (preserving comments & blanks).
 *  2. Allow interactive update of 'unit' & 'theme'.
 *  3. Load derived file contents (Config auto-loads *address → *Contents).
 *  4. Process prose sections through InlineCodeProcessor (inline/backtick code formatting).
 *  5. Highlight code samples & assignment code with Highlighter using selected theme.
 *  6. (Optional) Execute assignment Java code (runJavaFile) and include output if available.
 *  7. Assemble final HTML discussion post and write it to output_file_address (resolved).
 *
 * Notes:
 *  - No external CSS; all inline styles for portability.
 *  - Derived config keys are created by replacing 'address'→'contents' and snake_case→camelCase.
 *  - Highlighter has its own internal theme names; we pass config theme directly.
 */
public class DiscussionPostFormatter {

    public static void main(String[] args) throws Exception {
        Config config = Config.load("config.txt");

        // --- Interactive update of unit/theme (existing behavior) ---
        String currentUnit = config.get("unit");
        String currentTheme = config.get("theme");

        System.out.println("Current unit  : " + currentUnit);
        System.out.println("Current theme : " + currentTheme);
        System.out.println();

        String newUnit = prompt("Enter new unit (digits only, Enter to keep): ");
        String chosenTheme = chooseTheme(currentTheme); // may return currentTheme

        boolean changed = false;

        if (newUnit != null && !newUnit.isBlank() && !newUnit.equals(currentUnit)) {
            if (newUnit.matches("\\d+")) {
                config.set("unit", newUnit);
                changed = true;
            } else {
                System.out.println("Ignoring invalid unit (must be digits).");
            }
        }

        if (chosenTheme != null && !chosenTheme.equals(currentTheme)) {
            config.set("theme", chosenTheme);
            changed = true;
        }

        if (changed) {
            config.save();
            System.out.println("Configuration updated and saved to config.txt.");
            config = config.reload();
        } else {
            System.out.println("No changes made.");
        }

        System.out.println();
        System.out.println(config.toString());
        System.out.println(config.toResolvedString());

        // --- Load theme via ThemeLoader (optional separate system) ---
        String activeThemeName = config.get("theme");
        Theme activeTheme = ThemeLoader.load(activeThemeName);
        if (activeTheme != null) {
            System.out.println("Loaded theme '" + activeTheme.getName() + "' (" +
                    activeTheme.getStyles().size() + " style tokens)");
        } else {
            System.out.println("Theme file not found / not parsed for '" + activeThemeName +
                    "' (Highlighter still uses its built-in theme list).");
        }

        String resolvedOutputPath = config.getResolved("output_file_address");
        System.out.println("Resolved output_file_address: " + resolvedOutputPath);

        // --- Build discussion HTML ---
        String html = generateDiscussionHtml(config, activeThemeName, /*runExecution*/ true);

        // --- Write output ---
        try {
            Utils.writeFile(resolvedOutputPath, html);
            System.out.println("Wrote discussion post HTML to: " + resolvedOutputPath);
        } catch (Exception e) {
            System.out.println("Failed to write output HTML: " + e.getMessage());
        }
    }

    /**
     * Generate the full HTML for the discussion post.
     *
     * @param config           Loaded configuration
     * @param themeName        Theme name for Highlighter
     * @param runExecution     If true, attempts to compile & run the code file (append output)
     * @return HTML string
     */
    private static String generateDiscussionHtml(Config config,
                                                 String themeName,
                                                 boolean runExecution) {
        String unit = safe(config.get("unit"));

        // --- Fetch derived contents (Config already loaded file contents) ---
        String assignmentText = safe(config.get("assignmentTextFileContents"));
        String introText = safe(config.get("introductionTextFileContents"));
        String explanation1 = safe(config.get("explanation1TextFileContents"));
        String explanation2 = safe(config.get("explanation2TextFileContents"));
        String assignmentQuestion = safe(config.get("assignmentTextForDiscussionQuestionFileContents"));
        String discussionQuestion = safe(config.get("discussionQuestionFileContents"));
        String references = safe(config.get("referencesFileContents"));
        String sampleCode = safe(config.get("assignmentSampleCodeFileContents"));
        String codeSource = safe(config.get("codeFileContents"));
        String compilerMessages = safe(config.get("compilerMessagesFileContents"));
        String capturedProgramOutput = safe(config.get("programOutputFileContents")); // may be empty

        // --- Process prose via InlineCodeProcessor (inline formatting) ---
        introText = InlineCodeProcessor.process(introText);
        assignmentText = InlineCodeProcessor.process(assignmentText);
        explanation1 = InlineCodeProcessor.process(explanation1);
        explanation2 = InlineCodeProcessor.process(explanation2);
        assignmentQuestion = InlineCodeProcessor.process(assignmentQuestion);
        discussionQuestion = InlineCodeProcessor.process(discussionQuestion);
        references = InlineCodeProcessor.process(references);
        compilerMessages = InlineCodeProcessor.process(compilerMessages);

        // --- Highlight source & sample code (if present) ---
        String highlightedSampleCode = sampleCode.isBlank()
                ? "(No sample code provided.)"
                : Highlighter.highlight(sampleCode, themeName);

        String highlightedAssignmentCode = codeSource.isBlank()
                ? "(No assignment code provided.)"
                : Highlighter.highlight(codeSource, themeName);

        // --- Optionally run the assignment code and capture fresh output ---
        String freshExecutionOutput = "";
        if (runExecution) {
            String resolvedCodePath = config.getResolved("code_file_address");
            if (resolvedCodePath != null && Utils.fileExists(resolvedCodePath)) {
                try {
                    freshExecutionOutput = Utils.runJavaFile(resolvedCodePath);
                } catch (Exception e) {
                    freshExecutionOutput = "[Execution failed] " + e.getMessage();
                }
            } else {
                freshExecutionOutput = "[Code file not found or unreadable]";
            }
        }

        // --- Basic inline style (all inline per constraints) ---
        StringBuilder html = new StringBuilder(32_000);
        html.append("<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>")
            .append("<title>Unit ").append(escape(unit)).append(" Discussion Post</title>")
            .append("<meta name='viewport' content='width=device-width,initial-scale=1'>")
            .append("</head><body style=\"font-family:Arial,Helvetica,sans-serif;line-height:1.5;margin:2rem;\">");

        html.append(sectionHeader("Unit " + escape(unit) + " Discussion Post"))
            .append(paragraph(introText));

        html.append(sectionHeader("Assignment Overview"))
            .append(paragraph(assignmentText));

        html.append(sectionHeader("Primary Explanation"))
            .append(paragraph(explanation1));

        if (!explanation2.isBlank()) {
            html.append(sectionHeader("Additional Explanation"))
                .append(paragraph(explanation2));
        }

        html.append(sectionHeader("Discussion Question Context"))
            .append(paragraph(assignmentQuestion));

        html.append(sectionHeader("Discussion Question"))
            .append(blockQuote(discussionQuestion));

        html.append(sectionHeader("Sample Code"))
            .append(highlightedSampleCode);

        html.append(sectionHeader("Assignment Code"))
            .append(highlightedAssignmentCode);

        // Show existing compiler messages if any
        if (!compilerMessages.isBlank()) {
            html.append(sectionHeader("Compiler Messages"))
                .append(preBlock(compilerMessages));
        }

        // Show stored output file (if any) and fresh execution output (if run)
        if (!capturedProgramOutput.isBlank()) {
            html.append(sectionHeader("Previously Captured Program Output"))
                .append(preBlock(capturedProgramOutput));
        }

        html.append(sectionHeader("Current Execution Output"))
            .append(preBlock(freshExecutionOutput.isBlank()
                    ? "[No execution performed or no output]"
                    : freshExecutionOutput));

        if (!references.isBlank()) {
            html.append(sectionHeader("References"))
                .append(paragraph(references));
        }

        html.append("<hr style='margin:2rem 0;'>")
            .append("<p style='font-size:0.8rem;color:#666;'>Generated automatically by DiscussionPostFormatter using Config, InlineCodeProcessor, Highlighter, and Utils.</p>")
            .append("</body></html>");

        return html.toString();
    }

    /* ---------- Small HTML helpers (inline style only) ---------- */

    private static String sectionHeader(String text) {
        return "<h2 style=\"margin-top:2.2rem;margin-bottom:0.6rem;font-size:1.35rem;border-bottom:1px solid #ccc;padding-bottom:0.3rem;\">" +
                escape(text) + "</h2>";
    }

    private static String paragraph(String htmlAlreadyProcessed) {
        if (htmlAlreadyProcessed == null || htmlAlreadyProcessed.isBlank()) return "";
        return "<p style=\"margin:0.9rem 0;\">" + htmlAlreadyProcessed + "</p>";
    }

    private static String blockQuote(String htmlAlreadyProcessed) {
        if (htmlAlreadyProcessed == null || htmlAlreadyProcessed.isBlank()) return "";
        return "<blockquote style=\"margin:1rem 1.5rem;padding:0.6rem 1rem;border-left:4px solid #888;background:#f9f9f9;\">" +
                htmlAlreadyProcessed + "</blockquote>";
    }

    private static String preBlock(String text) {
        return "<pre style=\"background:#f5f5f5;padding:0.8rem;border:1px solid #ccc;overflow:auto;font-family:'Courier New',monospace;font-size:0.85rem;line-height:1.35;\">" +
                escape(text) + "</pre>";
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"","&quot;")
                .replace("'", "&#39;");
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    /* ---------- Theme selection (existing logic retained) ---------- */

    private static String chooseTheme(String currentTheme) throws Exception {
        var themes = java.util.List.of(Highlighter.availableThemes());
        if (themes.isEmpty()) {
            String simple = prompt("Enter new theme (Enter to keep current '" + currentTheme + "'): ");
            if (simple == null || simple.isBlank()) return currentTheme;
            return simple.trim();
        }

        System.out.println("Available (built-in) highlighter themes:");
        for (int i = 0; i < themes.size(); i++) {
            System.out.printf("  %d) %s%s%n", i + 1, themes.get(i),
                    themes.get(i).equalsIgnoreCase(currentTheme) ? " (current)" : "");
        }
        System.out.println();

        String input = prompt("Enter theme number or name (Enter to keep '" + currentTheme + "'): ");
        if (input == null || input.isBlank()) return currentTheme;
        input = input.trim();

        if (input.matches("\\d+")) {
            int idx = Integer.parseInt(input) - 1;
            if (idx >= 0 && idx < themes.size()) {
                String chosen = themes.get(idx);
                System.out.println("Selected theme: " + chosen);
                return chosen;
            } else {
                System.out.println("Invalid theme number; keeping current theme.");
                return currentTheme;
            }
        }

        for (String t : themes) {
            if (t.equalsIgnoreCase(input)) {
                System.out.println("Selected theme: " + t);
                return t;
            }
        }

        System.out.println("No matching theme name; keeping current theme.");
        return currentTheme;
    }

    private static String prompt(String msg) throws Exception {
        System.out.print(msg);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        return br.readLine();
    }
}