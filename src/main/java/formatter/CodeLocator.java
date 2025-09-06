// Current filename: CodeLocator.java

package formatter;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Minimal fallback locator for the Java source file.
 *
 * Priority:
 *  1. Use resolved code_file_address if it points to a file.
 *  2. Else scan under config directory for Unit_<UNIT_NUMBER>/Discussion_Assignment.java
 *
 * No new config keys required.
 */
public final class CodeLocator {

    private CodeLocator() {}

    public static Path locate(Config config) {
        String resolved = config.getResolved("code_file_address");
        if (resolved != null && !resolved.isBlank()) {
            Path explicit = resolveAgainstConfigDir(config, resolved);
            if (Files.isRegularFile(explicit)) {
                return explicit.normalize();
            }
        }
        // fallback scan
        String unit = config.get("unit");
        if (unit == null || unit.isBlank()) return null;

        Path root = config.getConfigDir();
        String needle = "Unit_" + unit + "/Discussion_Assignment.java";
        List<Path> matches = new ArrayList<>();
        try {
            Files.walk(root)
                 .filter(p -> p.toString().endsWith("Discussion_Assignment.java"))
                 .forEach(p -> {
                     String unixRel = root.relativize(p).toString().replace('\\','/');
                     if (unixRel.contains(needle)) {
                         matches.add(p);
                     }
                 });
        } catch (IOException e) {
            System.out.println("Code scan failed: " + e.getMessage());
        }

        if (matches.isEmpty()) return null;
        if (matches.size() == 1) return matches.get(0);

        // heuristic: choose shortest path string
        Path best = matches.get(0);
        for (Path p : matches) {
            if (p.toString().length() < best.toString().length()) best = p;
        }
        System.out.println("Multiple code files detected; choosing: " + best);
        return best;
    }

    private static Path resolveAgainstConfigDir(Config config, String raw) {
        Path p = Paths.get(raw);
        if (!p.isAbsolute()) {
            p = config.getConfigDir().resolve(raw);
        }
        return p.normalize();
    }
}