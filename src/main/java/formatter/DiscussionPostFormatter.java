package formatter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Entry point for building a discussion post HTML output.
 */
public class DiscussionPostFormatter {

    public static void main(String[] args) throws Exception {
        // Simple demo input sources (replace with real inputs or CLI args)
        String assignmentText = """
                Implement a program that prints "Hello, world!" and then the sum of two numbers.
                Use clean code principles.
                """;

        // Pretend we loaded a Java source file
        String javaSource = """
                public class Hello {
                    public static void main(String[] args) {
                        // Print greeting
                        System.out.println("Hello, world!");
                        int a = 5;
                        int b = 7;
                        System.out.println("Sum = " + (a + b));
                    }
                }
                """;

        // Simulated program output
        String programOutput = """
                Hello, world!
                Sum = 12
                """;

        // 1. Optionally process inline code markers inside assignment text
        String processedAssignment = InlineCodeProcessor.process(assignmentText);

        // 2. Highlight code (choose a theme: tango, monokai, dark, light)
        String highlighted = Highlighter.highlight(javaSource, "tango");

        // 3. Build content blocks
        List<ContentBlock> blocks = List.of(
                new ContentBlock.DefaultContentBlock(ContentBlock.Type.ASSIGNMENT_TEXT, processedAssignment),
                new ContentBlock.DefaultContentBlock(ContentBlock.Type.HIGHLIGHTED_CODE, highlighted),
                new ContentBlock.DefaultContentBlock(ContentBlock.Type.PROGRAM_OUTPUT, programOutput),
                new ContentBlock.DefaultContentBlock(ContentBlock.Type.EXPLANATION_PLACEHOLDER, "")
        );

        // 4. Assemble full HTML
        String html = HtmlAssembler.assemble(blocks);

        // 5. Write to file (or stdout)
        Path out = Path.of("discussion_post.html");
        Files.writeString(out, html);

        System.out.println("Generated: " + out.toAbsolutePath());
    }
}