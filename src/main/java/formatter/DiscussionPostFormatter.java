// File: DiscussionPostFormatter.java

package formatter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class DiscussionPostFormatter {

    public static void main(String[] args) throws Exception {
        Config config = Config.load("config.txt");

        String currentUnit = config.get("unit");
        String currentTheme = config.get("theme");

        System.out.println("Config directory : " + config.getConfigDir());
        System.out.println("Current unit     : " + currentUnit);
        System.out.println("Current theme    : " + currentTheme);
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
            System.out.println("Configuration updated and saved.");
            config = config.reload();
        }

        Path codePath = CodeLocator.locate(config);
        if (codePath == null) {
            System.out.println("No code file located (explicit path missing and fallback failed).");
        } else {
            System.out.println("Using code file: " + codePath);
        }

        System.out.println();
        System.out.println(config.toString());
        System.out.println(config.toResolvedString());

        String activeThemeName = config.get("theme");
        Theme activeTheme = ThemeLoader.load(activeThemeName);
        if (activeTheme != null) {
            System.out.println("Loaded external theme '" + activeTheme.getName() +
                    "' (" + activeTheme.getStyles().size() + " style tokens)");
        } else {
            System.out.println("Using built-in palette for theme '" + activeThemeName + "'.");
        }

        String resolvedOutputPath = config.getResolved("output_file_address");
        System.out.println("Output file (resolved): " + resolvedOutputPath);

        String htmlRaw = generateDiscussionHtml(config, activeThemeName, true, codePath);
        String htmlFinal = HtmlBeautifier.maybeBeautify(config, htmlRaw);
        boolean beautified = (htmlFinal != htmlRaw);
        System.out.println(beautified
                ? "HTML beautification applied."
                : "HTML beautification skipped or produced no changes.");

        try {
            Utils.writeFile(resolvedOutputPath, htmlFinal);
            System.out.println("Wrote discussion post HTML.");
        } catch (Exception e) {
            System.out.println("Failed to write output HTML: " + e.getMessage());
        }
    }

    private static String generateDiscussionHtml(Config config,
                                                 String themeName,
                                                 boolean runExecution,
                                                 Path codePath) {
        String unit = safe(config.get("unit"));

        // Derived content (may now contain diagnostic markers)
        String assignmentText = safe(config.get("assignmentTextFileContents", true));
        String introText = safe(config.get("introductionTextFileContents", true));
        String explanation1 = safe(config.get("explanation1TextFileContents", true));
        String explanation2 = safe(config.get("explanation2TextFileContents", true));
        String assignmentQuestion = safe(config.get("assignmentTextForDiscussionQuestionFileContents", true));
        String discussionQuestion = safe(config.get("discussionQuestionFileContents", true));
        String references = safe(config.get("referencesFileContents", true));
        String sampleCode = safe(config.get("assignmentSampleCodeFileContents", true));
        String codeSource = safe(config.get("codeFileContents", true));
        String compilerMessagesPrev = safe(config.get("compilerMessagesFileContents", true));
        String capturedProgramOutputPrev = safe(config.get("programOutputFileContents", true));

        // Inline code processing (skip markers)
        assignmentText = processIfNotDiagnostic(assignmentText);
        introText = processIfNotDiagnostic(introText);
        explanation1 = processIfNotDiagnostic(explanation1);
        explanation2 = processIfNotDiagnostic(explanation2);
        assignmentQuestion = processIfNotDiagnostic(assignmentQuestion);
        discussionQuestion = processIfNotDiagnostic(discussionQuestion);
        references = processIfNotDiagnostic(references);
        compilerMessagesPrev = processIfNotDiagnostic(compilerMessagesPrev);

        boolean codeIsDiagnostic = isDiagnosticMarker(codeSource);
        String highlightedAssignmentCode;
        if (codeIsDiagnostic) {
            highlightedAssignmentCode = "<pre style=\"background:#fff3f3;padding:0.8rem;border:1px solid #d99;\">" +
                    escape(codeSource) + "</pre>";
        } else {
            highlightedAssignmentCode = codeSource.isBlank()
                    ? "(No assignment code provided.)"
                    : Highlighter.highlight(codeSource, themeName);
        }

        // Compile / run
        String currentCompilerMessagesReport;
        String currentProgramOutputReport;

        if (runExecution && codePath != null && Files.isRegularFile(codePath) && !codeIsDiagnostic) {
            Utils.ExecutionResult er;
            try {
                er = Utils.runJavaFileDetailed(codePath.toString());
            } catch (Exception e) {
                er = new Utils.ExecutionResult(false, "[Invocation error] " + e.getMessage(), "");
            }
            currentCompilerMessagesReport = buildCompilerReport(er.compilerMessages(), er.compiled());
            if (er.compiled()) {
                currentProgramOutputReport = buildProgramOutputReport(er.programOutput());
            } else {
                currentProgramOutputReport = "[No program output (compilation failed)]";
            }
        } else if (codePath == null) {
            currentCompilerMessagesReport = "[No code file located]";
            currentProgramOutputReport = "[No program output (no code file)]";
        } else if (codeIsDiagnostic) {
            currentCompilerMessagesReport = "[Skipped execution due to diagnostic: " + codeSource + "]";
            currentProgramOutputReport = "[Skipped execution due to diagnostic]";
        } else {
            currentCompilerMessagesReport = "[Execution disabled]";
            currentProgramOutputReport = "[Program output collection disabled]";
        }

        StringBuilder html = new StringBuilder(32_000);
        // OPEN: add <main> wrapper
        html.append("<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>")
            .append("<title>Unit ").append(escape(unit)).append(" Discussion Post</title>")
            .append("<meta name='viewport' content='width=device-width,initial-scale=1'>")
            .append("</head><body style=\"font-family:Arial,Helvetica,sans-serif;line-height:1.5;margin:2rem;\">")
            .append("<main style='display:block;width:100%;max-width:960px;margin:0 auto;'>");

        html.append(sectionHeader("Unit " + escape(unit) + " Discussion Post"));

        // Assignment Overview
        appendConditionalSection(html, config, "include_assignment_text", "Assignment Overview", assignmentText, true);

        // Assignment Code Sample
        appendConditionalSection(html, config, "include_sample_code", "Assignment Code Sample", sampleCode, false, true);

        // Introduction
        appendConditionalSection(html, config, "include_introduction", "Introduction", introText, false);

        // Primary Explanation
        appendConditionalSection(html, config, "include_explanation1", "Primary Explanation", explanation1, false);

        // Additional Explanation
        appendConditionalSection(html, config, "include_explanation2", "Additional Explanation", explanation2, false);

        // Discussion Question Context
        appendConditionalSection(html, config, "include_assignment_text_for_discussion_question", "Discussion Question Context", assignmentQuestion, true);

        // Discussion Question
        appendConditionalSection(html, config, "include_discussion_question", "Discussion Question", discussionQuestion, false);

        // Code Listing
        if (enabled(config, "include_code_listing")) {
            html.append(sectionHeader("Assigned Code Work"))
                .append(highlightedAssignmentCode);
        } else {
            logSkip(config, "include_code_listing", highlightedAssignmentCode);
        }

        // Compiler Messages (previous + current)
        if (enabled(config, "include_compiler_messages")) {
            if (!compilerMessagesPrev.isBlank()) {
                html.append(sectionHeader("Previously Captured Compiler Messages"))
                    .append(preBlock(compilerMessagesPrev));
            } else if (isDiagnosticMarker(compilerMessagesPrev)) {
                html.append(sectionHeader("Previously Captured Compiler Messages"))
                    .append(preBlock(compilerMessagesPrev));
            }
            html.append(sectionHeader("Current Compilation Messages (This Run)"))
                .append(preBlock(currentCompilerMessagesReport));
        } else {
            logSkip(config, "include_compiler_messages", compilerMessagesPrev + currentCompilerMessagesReport);
        }

        // Program Output (previous + current)
        if (enabled(config, "include_program_output")) {
            if (!capturedProgramOutputPrev.isBlank()) {
                html.append(sectionHeader("Previously Captured Program Output"))
                    .append(preBlock(capturedProgramOutputPrev));
            } else if (isDiagnosticMarker(capturedProgramOutputPrev)) {
                html.append(sectionHeader("Previously Captured Program Output"))
                    .append(preBlock(capturedProgramOutputPrev));
            }
            html.append(sectionHeader("Current Program Output (This Run)"))
                .append(preBlock(currentProgramOutputReport));
        } else {
            logSkip(config, "include_program_output", capturedProgramOutputPrev + currentProgramOutputReport);
        }

        // References
        appendConditionalSection(html, config, "include_references", "References", references, false);

        // CLOSE: footer + close </main>
        html.append("<footer style='margin-top:3rem;font-size:0.7rem;color:#555;opacity:0.85;text-align:center;'>")
            .append("<!-- assembler: v2025-09-07 | Formatted by Jemz using Discussion Post Formatter -->")
            .append(" | Created by RuffusReeves with the assistance of GitHub Copilot")
            .append("</footer>")
            .append("</main>")
            .append("</body></html>");

        return html.toString();
    }

    /* -------- Section helper with diagnostics -------- */

    private static void appendConditionalSection(StringBuilder html,
                                                 Config config,
                                                 String toggleKey,
                                                 String heading,
                                                 String content,
                                                 boolean italicize) {
        appendConditionalSection(html, config, toggleKey, heading, content, italicize, false);
    }

    private static void appendConditionalSection(StringBuilder html,
                                                 Config config,
                                                 String toggleKey,
                                                 String heading,
                                                 String content,
                                                 boolean italicize,
                                                 boolean forcePre) {
        if (!enabled(config, toggleKey)) {
            logSkip(config, toggleKey, content);
            return;
        }
        if (isDiagnosticMarker(content)) {
            html.append(sectionHeader(heading))
                .append(diagnosticParagraph(content));
            return;
        }
        if (content.isBlank()) {
            html.append(sectionHeader(heading))
                .append("<p style='margin:0.6rem 0;color:#777;font-style:italic;'>No content (empty file).</p>");
            return;
        }
        html.append(sectionHeader(heading));
        if (forcePre) {
            html.append(italicize ? italicPreBlock(content) : preBlock(content));
        } else if (italicize) {
            html.append(italic(content));
        } else {
            html.append(paragraph(content));
        }
    }

    private static boolean isDiagnosticMarker(String s) {
        return Config.isMissingMarker(s) || Config.isUnreadableMarker(s);
    }

    private static String diagnosticParagraph(String marker) {
        String style = "margin:0.6rem 0;padding:0.75rem;border:1px solid #e0b4b4;background:#fff5f5;color:#922; font-size:0.9rem;";
        return "<div style='" + style + "'><strong>File Issue:</strong> " + escape(marker) + "</div>";
    }

    private static String processIfNotDiagnostic(String s) {
        if (isDiagnosticMarker(s)) return s;
        return InlineCodeProcessor.process(s);
    }

    /* -------- Toggle Helpers -------- */

    private static boolean enabled(Config config, String key) {
        String v = config.get(key);
        if (v == null) return true;
        return !v.trim().equalsIgnoreCase("false");
    }

    private static void logSkip(Config config, String key, String content) {
        String dbg = config.get("tidy_debug");
        boolean debug = (dbg != null && dbg.equalsIgnoreCase("true"));
        if (debug) {
            System.out.println("[Skip] " + key + " = false OR content blank (length=" +
                    (content == null ? 0 : content.length()) + ")");
        }
    }

    /* -------- Reporting helpers -------- */

    private static String buildCompilerReport(String rawMessages, boolean compiled) {
        if (rawMessages == null || rawMessages.isBlank()) {
            return "[No compiler messages]";
        }
        String[] lines = rawMessages.split("\\R");
        int nonBlank = 0;
        for (String l : lines) if (!l.isBlank()) nonBlank++;
        StringBuilder sb = new StringBuilder();
        sb.append(compiled ? "[Compilation succeeded]" : "[Compilation failed]");
        sb.append(" Diagnostics: ").append(nonBlank).append(" line");
        if (nonBlank != 1) sb.append('s');
        sb.append('\n').append(rawMessages.trim());
        return sb.toString();
    }

    private static String buildProgramOutputReport(String rawOutput) {
        if (rawOutput == null || rawOutput.isBlank()) {
            return "[No program output]";
        }
        String[] lines = rawOutput.split("\\R");
        int nonBlank = 0;
        for (String l : lines) if (!l.isBlank()) nonBlank++;
        return "[Program output] Lines: " + nonBlank + "\n" + rawOutput.trim();
    }

    /* -------- Theme selection -------- */

    private static String chooseTheme(String currentTheme) throws Exception {
        List<String> builtIns = Arrays.asList(Highlighter.availableThemes());
        List<String> external = ThemeLoader.listAvailableThemeNames();

        LinkedHashSet<String> merged = new LinkedHashSet<>();
        merged.addAll(external);
        merged.addAll(builtIns);
        List<String> themeList = new ArrayList<>(merged);

        if (themeList.isEmpty()) {
            String simple = prompt("Enter new theme (Enter to keep '" + currentTheme + "'): ");
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

    /* -------- HTML helpers (block-aware) -------- */

    private static String sectionHeader(String text) {
        return "<h2 style=\"margin-top:2.2rem;margin-bottom:0.6rem;font-size:1.35rem;border-bottom:1px solid #ccc;padding-bottom:0.3rem;\">" +
                escape(text) + "</h2>";
    }

    private static boolean containsBlockHtml(String html) {
        if (html == null) return false;
        String s = html;
        // Common block-level tags; case-insensitive
        return s.matches("(?is).*<\\s*(div|p|h[1-6]|ul|ol|li|pre|section|article|header|footer|nav|table|thead|tbody|tr|td|th|blockquote|figure|figcaption)\\b.*");
    }

    private static String paragraph(String htmlAlreadyProcessed) {
        if (htmlAlreadyProcessed == null || htmlAlreadyProcessed.isBlank()) return "";
        if (containsBlockHtml(htmlAlreadyProcessed)) {
            // Avoid invalid nesting: don't put blocks inside <p>
            return "<div style='margin:0.9rem 0;'>" + htmlAlreadyProcessed + "</div>";
        }
        return "<p style='margin:0.9rem 0;'>" + htmlAlreadyProcessed + "</p>";
    }

    private static String italic(String htmlAlreadyProcessed) {
        if (htmlAlreadyProcessed == null || htmlAlreadyProcessed.isBlank()) return "";
        if (containsBlockHtml(htmlAlreadyProcessed)) {
            return "<div style='margin:0.9rem 0;font-style:italic;'>" + htmlAlreadyProcessed + "</div>";
        }
        return "<p style='margin:0.9rem 0;font-style:italic;'>" + htmlAlreadyProcessed + "</p>";
    }

    private static String preBlock(String text) {
        return "<pre style=\"background:#f5f5f5;padding:0.8rem;border:1px solid #ccc;overflow:auto;font-family:'Courier New',monospace;font-size:0.85rem;line-height:1.35;white-space:pre-wrap;\">"
                + escape(text) + "</pre>";
    }

    private static String italicPreBlock(String text) {
        return "<pre style=\"background:#f5f5f5;padding:0.8rem;border:1px solid #ccc;overflow:auto;font-style:italic;font-family:'Courier New',monospace;font-size:0.85rem;line-height:1.35;white-space:pre-wrap;\">"
                + escape(text) + "</pre>";
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;")
                .replace("<","&lt;")
                .replace(">","&gt;")
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