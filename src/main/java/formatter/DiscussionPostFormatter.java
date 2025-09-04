package formatter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Main program for assembling the discussion post HTML.
 * Now includes ephemeral (non-persisted) section "Current Compilation Messages"
 * capturing javac diagnostics from this run.
 */
public class DiscussionPostFormatter {

    public static void main(String[] args) throws Exception {
        Config config = Config.load("config.txt");

        String currentUnit = config.get("unit");
        String currentTheme = config.get("theme");

        System.out.println("Current unit  : " + currentUnit);
        System.out.println("Current theme : " + currentTheme);
        System.out.println();

        String newUnit = prompt("Enter new unit (digits only, Enter to keep): ");
        String chosenTheme = chooseTheme(currentTheme);

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

        String activeThemeName = config.get("theme");
        Theme activeTheme = ThemeLoader.load(activeThemeName);
        if (activeTheme != null) {
            System.out.println("Loaded external theme '" + activeTheme.getName() + "' (" +
                    activeTheme.getStyles().size() + " style tokens)");
        } else {
            System.out.println("Using built-in palette for theme '" + activeThemeName + "'.");
        }

        String resolvedOutputPath = config.getResolved("output_file_address");
        System.out.println("Resolved output_file_address: " + resolvedOutputPath);

        String html = generateDiscussionHtml(config, activeThemeName, true);

        try {
            Utils.writeFile(resolvedOutputPath, html);
            System.out.println("Wrote discussion post HTML to: " + resolvedOutputPath);
        } catch (Exception e) {
            System.out.println("Failed to write output HTML: " + e.getMessage());
        }
    }

    private static String generateDiscussionHtml(Config config,
                                                 String themeName,
                                                 boolean runExecution) {
        String unit = safe(config.get("unit"));

        // Derived contents (previous run / static inputs)
        String assignmentText = safe(config.get("assignmentTextFileContents"));
        String introText = safe(config.get("introductionTextFileContents"));
        String explanation1 = safe(config.get("explanation1TextFileContents"));
        String explanation2 = safe(config.get("explanation2TextFileContents"));
        String assignmentQuestion = safe(config.get("assignmentTextForDiscussionQuestionFileContents"));
        String discussionQuestion = safe(config.get("discussionQuestionFileContents"));
        String references = safe(config.get("referencesFileContents"));
        String sampleCode = safe(config.get("assignmentSampleCodeFileContents"));
        String codeSource = safe(config.get("codeFileContents"));
        String compilerMessagesPrev = safe(config.get("compilerMessagesFileContents"));
        String capturedProgramOutputPrev = safe(config.get("programOutputFileContents"));

        // Inline code formatting
        assignmentText = InlineCodeProcessor.process(assignmentText);
        introText = InlineCodeProcessor.process(introText);
        explanation1 = InlineCodeProcessor.process(explanation1);
        explanation2 = InlineCodeProcessor.process(explanation2);
        assignmentQuestion = InlineCodeProcessor.process(assignmentQuestion);
        discussionQuestion = InlineCodeProcessor.process(discussionQuestion);
        references = InlineCodeProcessor.process(references);
        compilerMessagesPrev = InlineCodeProcessor.process(compilerMessagesPrev);

        // Syntax highlight assignment code
        String highlightedAssignmentCode = codeSource.isBlank()
                ? "(No assignment code provided.)"
                : Highlighter.highlight(codeSource, themeName);

        // Ephemeral (this run) compilation + execution
        String currentCompilerMessagesReport;
        String freshExecutionOutput;
        if (runExecution) {
            String resolvedCodePath = config.getResolved("code_file_address");
            if (resolvedCodePath != null && Utils.fileExists(resolvedCodePath)) {
                Utils.ExecutionResult er;
                try {
                    er = Utils.runJavaFileDetailed(resolvedCodePath);
                } catch (Exception e) {
                    er = new Utils.ExecutionResult(false, "[Invocation error] " + e.getMessage(), "");
                }
                currentCompilerMessagesReport = buildCompilerReport(er.compilerMessages(), er.compiled());
                freshExecutionOutput = er.compiled()
                        ? (er.programOutput().isBlank() ? "[Program produced no output]" : er.programOutput())
                        : "[No execution due to compilation failure]";
            } else {
                currentCompilerMessagesReport = "[Source file not found or unreadable]";
                freshExecutionOutput = "[Code file not found or unreadable]";
            }
        } else {
            currentCompilerMessagesReport = "[Execution disabled]";
            freshExecutionOutput = "[Execution skipped]";
        }

        // Build HTML
        StringBuilder html = new StringBuilder(32_000);
        html.append("<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>")
            .append("<title>Unit ").append(escape(unit)).append(" Discussion Post</title>")
            .append("<meta name='viewport' content='width=device-width,initial-scale=1'>")
            .append("</head><body style=\"font-family:Arial,Helvetica,sans-serif;line-height:1.5;margin:2rem;\">");

        html.append(sectionHeader("Unit " + escape(unit) + " Discussion Post"));

        html.append(sectionHeader("Assignment Overview"))
            .append(italic(assignmentText));

        html.append(sectionHeader("Assignment Code Sample"))
            .append(italicPreBlock(sampleCode));

        html.append(sectionHeader("Introduction"))
            .append(paragraph(introText));

        html.append(sectionHeader("Primary Explanation"))
            .append(paragraph(explanation1));

        if (!explanation2.isBlank()) {
            html.append(sectionHeader("Additional Explanation"))
                .append(paragraph(explanation2));
        }

        html.append(sectionHeader("Discussion Question Context"))
            .append(italic(assignmentQuestion));

        html.append(sectionHeader("Discussion Question"))
            .append(paragraph(discussionQuestion));

        html.append(sectionHeader("Assigned Code Work"))
            .append(highlightedAssignmentCode);

        // Previous (persisted) compiler messages if any
        if (!compilerMessagesPrev.isBlank()) {
            html.append(sectionHeader("Previously Captured Compiler Messages"))
                .append(preBlock(compilerMessagesPrev));
        }

        // Current compilation (non-persisted)
        html.append(sectionHeader("Current Compilation Messages (This Run)"))
            .append(preBlock(currentCompilerMessagesReport));

        // Previous (persisted) program output if any
        if (!capturedProgramOutputPrev.isBlank()) {
            html.append(sectionHeader("Previously Captured Program Output"))
                .append(preBlock(capturedProgramOutputPrev));
        }

        html.append(sectionHeader("Current Execution Output"))
            .append(preBlock(freshExecutionOutput));

        if (!references.isBlank()) {
            html.append(sectionHeader("References"))
                .append(paragraph(references));
        }

        html.append("<hr style='margin:2rem 0;'>")
            .append("<p style='font-size:0.8rem;color:#666;'>Generated automatically by DiscussionPostFormatter using Config, InlineCodeProcessor, Highlighter, and Utils.</p>")
            .append("</body></html>");

        return html.toString();
    }

    /**
     * Build a human-friendly compiler messages report:
     *  - If empty => "[No compiler messages]"
     *  - Otherwise adds a summary line with counts.
     */
    private static String buildCompilerReport(String rawMessages, boolean compiled) {
        if (rawMessages == null || rawMessages.isBlank()) {
            return "[No compiler messages]";
        }
        // Count non-blank lines
        String[] lines = rawMessages.split("\\R");
        int nonBlank = 0;
        for (String l : lines) {
            if (!l.isBlank()) nonBlank++;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(compiled ? "[Compilation succeeded]" : "[Compilation failed]");
        sb.append(" Diagnostics: ").append(nonBlank).append(" line");
        if (nonBlank != 1) sb.append('s');
        sb.append('\n').append(rawMessages.trim());
        return sb.toString();
    }

    /**
     * Theme selection unchanged from previous revision (external first).
     */
    private static String chooseTheme(String currentTheme) throws Exception {
        List<String> builtIns = Arrays.asList(Highlighter.availableThemes());
        List<String> external = ThemeLoader.listAvailableThemeNames();

        LinkedHashSet<String> merged = new LinkedHashSet<>();
        merged.addAll(external);
        merged.addAll(builtIns);
        List<String> themeList = new ArrayList<>(merged);

        if (themeList.isEmpty()) {
            String simple = prompt("Enter new theme (Enter to keep current '" + currentTheme + "'): ");
            if (simple == null || simple.isBlank()) return currentTheme;
            return simple.trim();
        }

        System.out.println("Available themes (external JSON + built-in):");
        for (int i = 0; i < themeList.size(); i++) {
            String name = themeList.get(i);
            boolean isExternal = external.contains(name);
            System.out.printf("  %2d) %s%s%s%n",
                    i + 1,
                    name,
                    isExternal ? " [ext]" : "",
                    name.equalsIgnoreCase(currentTheme) ? " (current)" : "");
        }
        System.out.println();

        String input = prompt("Enter theme number or name (Enter to keep '" + currentTheme + "'): ");
        if (input == null || input.isBlank()) return currentTheme;
        input = input.trim();

        if (input.matches("\\d+")) {
            int idx = Integer.parseInt(input) - 1;
            if (idx >= 0 && idx < themeList.size()) {
                String chosen = themeList.get(idx);
                System.out.println("Selected theme: " + chosen);
                return chosen;
            } else {
                System.out.println("Invalid theme number; keeping current theme.");
                return currentTheme;
            }
        }

        for (String t : themeList) {
            if (t.equalsIgnoreCase(input)) {
                System.out.println("Selected theme: " + t);
                return t;
            }
        }

        System.out.println("No matching theme name; keeping current theme.");
        return currentTheme;
    }

    /* ----------------- Formatting helpers ----------------- */

    private static String sectionHeader(String text) {
        return "<h2 style=\"margin-top:2.2rem;margin-bottom:0.6rem;font-size:1.35rem;border-bottom:1px solid #ccc;padding-bottom:0.3rem;\">" +
                escape(text) + "</h2>";
    }

    private static String paragraph(String htmlAlreadyProcessed) {
        if (htmlAlreadyProcessed == null || htmlAlreadyProcessed.isBlank()) return "";
        return "<p style=\"margin:0.9rem 0;\">" + htmlAlreadyProcessed + "</p>";
    }

    private static String italic(String htmlAlreadyProcessed) {
        if (htmlAlreadyProcessed == null || htmlAlreadyProcessed.isBlank()) return "";
        return "<p style=\"margin:0.9rem 0;font-style:italic;\">" + htmlAlreadyProcessed + "</p>";
    }

    private static String preBlock(String text) {
        return "<pre style=\"background:#f5f5f5;padding:0.8rem;border:1px solid #ccc;overflow:auto;font-family:'Courier New',monospace;font-size:0.85rem;line-height:1.35;\">" +
                escape(text) + "</pre>";
    }

    private static String italicPreBlock(String text) {
        return "<pre style=\"background:#f5f5f5;padding:0.8rem;border:1px solid #ccc;overflow:auto;font-style:italic;font-family:'Courier New',monospace;font-size:0.85rem;line-height:1.35;\">" +
                escape(text) + "</pre>";
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"","&quot;")
                .replace("'","&#39;");
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static String prompt(String msg) throws Exception {
        System.out.print(msg);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        return br.readLine();
    }
}