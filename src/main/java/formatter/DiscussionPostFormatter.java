package formatter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Prompts user to optionally change unit and theme.
 * If either changes, the config file is rewritten (comments preserved).
 * Loads the selected theme into memory (Theme) so Highlighter can use it later.
 */
public class DiscussionPostFormatter {

    public static void main(String[] args) throws Exception {
        Config config = Config.load("config.txt");

        String currentUnit = config.get("unit");
        String currentTheme = config.get("theme");

        System.out.println("Current unit  : " + currentUnit);
        System.out.println("Current theme : " + currentTheme);
        System.out.println();

        String newUnit  = prompt("Enter new unit (digits only, Enter to keep): ");
        String chosenTheme = chooseTheme(currentTheme); // may return currentTheme

        boolean changed = false;

        // Validate & apply unit
        if (newUnit != null && !newUnit.isBlank() && !newUnit.equals(currentUnit)) {
            if (newUnit.matches("\\d+")) {
                config.set("unit", newUnit);
                changed = true;
            } else {
                System.out.println("Ignoring invalid unit (must be digits).");
            }
        }

        // Apply theme (if changed)
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

        // Load the (possibly updated) theme so downstream code (Highlighter) can use it.
        String activeThemeName = config.get("theme");
        Theme activeTheme = ThemeLoader.load(activeThemeName);
        if (activeTheme != null) {
            System.out.println("Loaded theme '" + activeTheme.getName() + "' (" +
                    activeTheme.getStyles().size() + " style tokens)");
        } else {
            System.out.println("Failed to load theme '" + activeThemeName + "'.");
        }

        String outputResolved = config.getResolved("output_file_address");
        System.out.println("Resolved output_file_address: " + outputResolved);

        // Example (placeholder) of how a future Highlighter might be invoked:
        // Highlighter highlighter = new Highlighter(activeTheme);
        // String highlighted = highlighter.highlight(javaSourceString);
    }

    /**
     * List available themes in ./themes (files only). Returns chosen theme name
     * or the existing theme if user keeps it / input invalid / no themes found.
     */
    private static String chooseTheme(String currentTheme) throws Exception {
        List<String> themes = listThemeNames(Paths.get("themes"));

        if (themes.isEmpty()) {
            String simple = prompt("Enter new theme (Enter to keep current '" + currentTheme + "'): ");
            if (simple == null || simple.isBlank()) {
                return currentTheme;
            }
            return simple.trim();
        }

        System.out.println("Available themes:");
        for (int i = 0; i < themes.size(); i++) {
            System.out.printf("  %d) %s%s%n", i + 1, themes.get(i),
                    themes.get(i).equals(currentTheme) ? " (current)" : "");
        }
        System.out.println();

        String input = prompt("Enter theme number or name (Enter to keep '" + currentTheme + "'): ");
        if (input == null || input.isBlank()) {
            return currentTheme;
        }
        input = input.trim();

        // Try numeric selection
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

        // Try exact name match
        if (themes.contains(input)) {
            System.out.println("Selected theme: " + input);
            return input;
        } else {
            System.out.println("No matching theme name; keeping current theme.");
            return currentTheme;
        }
    }

    /**
     * Returns theme names (filename minus final extension) for regular readable files in the directory.
     */
    private static List<String> listThemeNames(Path themesDir) {
        List<String> names = new ArrayList<>();
        if (!Files.isDirectory(themesDir)) {
            return names;
        }
        try (var stream = Files.list(themesDir)) {
            stream.filter(p -> Files.isRegularFile(p) && Files.isReadable(p))
                  .forEach(p -> {
                      String file = p.getFileName().toString();
                      int dot = file.lastIndexOf('.');
                      String base = (dot > 0 ? file.substring(0, dot) : file);
                      if (!base.isBlank()) {
                          names.add(base);
                      }
                  });
        } catch (Exception e) {
            System.out.println("Warning: unable to list themes: " + e.getMessage());
        }
        return names;
    }

    private static String prompt(String msg) throws Exception {
        System.out.print(msg);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        return br.readLine();
    }
}