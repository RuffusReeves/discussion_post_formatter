package formatter;

import java.util.Arrays;
import java.util.Scanner;

public class DiscussionPostFormatter {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        // 1) Ask unit number
        System.out.print("Enter the unit number: ");
        String unitNumber = scanner.nextLine().trim();

        // 2) Load config with placeholder replacement
        Config config = Config.load("config.txt", unitNumber);

        // 3) Optional theme override
        String[] themes = Highlighter.availableThemes();
        Arrays.sort(themes);
        System.out.print("Enter theme (" + String.join(", ", themes) + ") or press Enter to use '" + config.theme + "': ");
        String themeInput = scanner.nextLine().trim();
        if (!themeInput.isEmpty()) {
            config.theme = themeInput;
        }

        scanner.close();

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
