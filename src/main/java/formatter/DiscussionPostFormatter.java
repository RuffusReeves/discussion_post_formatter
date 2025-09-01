package formatter;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Prompts user to optionally change unit and theme.
 * If either changes, the config file is rewritten (comments preserved).
 * Paths containing <UNIT_NUMBER> are resolved at usage time.
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
        String newTheme = prompt("Enter new theme (Enter to keep): ");

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

        // Apply theme (no placeholder handling now)
        if (newTheme != null && !newTheme.isBlank() && !newTheme.equals(currentTheme)) {
            config.set("theme", newTheme.trim());
            changed = true;
        }

        if (changed) {
            config.save();
            System.out.println("Configuration updated and saved to config.txt.");
            // Optional reload (not strictly required)
            config = config.reload();
        } else {
            System.out.println("No changes made.");
        }

        System.out.println();
        System.out.println(config.toString());
        System.out.println(config.toResolvedString());

        // Example of using a resolved path
        String outputResolved = config.getResolved("output_file_address");
        System.out.println("Resolved output_file_address: " + outputResolved);
    }

    private static String prompt(String msg) throws Exception {
        System.out.print(msg);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        return br.readLine();
    }
}