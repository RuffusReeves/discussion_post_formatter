package formatter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility methods for file I/O operations and Java program execution.
 * Package-root aware (fixed off-by-one parent bug).
 */
public class Utils {

    private static final Pattern PACKAGE_PATTERN =
            Pattern.compile("^\\s*package\\s+([a-zA-Z0-9_.]+)\\s*;");

    /* ---------- File helpers ---------- */

    public static String readFile(String path) throws IOException {
        return Files.readString(Paths.get(path), StandardCharsets.UTF_8);
    }

    public static void writeFile(String path, String content) throws IOException {
        Path filePath = Paths.get(path);
        Path parentDir = filePath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }
        Files.writeString(filePath, content, StandardCharsets.UTF_8);
    }

    public static boolean fileExists(String path) {
        if (path == null || path.trim().isEmpty()) return false;
        Path filePath = Paths.get(path);
        return Files.exists(filePath) && Files.isReadable(filePath);
    }

    public static String getFileExtension(String path) {
        if (path == null || path.trim().isEmpty()) return "";
        int lastDot = path.lastIndexOf('.');
        if (lastDot > 0 && lastDot < path.length() - 1) {
            return path.substring(lastDot + 1);
        }
        return "";
    }

    /* ---------- Public execution API ---------- */

    public static String runJavaFile(String codeFilePath) throws IOException, InterruptedException {
        ExecutionResult res = runJavaFileDetailed(codeFilePath);
        if (!res.compiled()) {
            return "[Compilation failed]\n" + res.compilerMessages();
        }
        return res.programOutput();
    }

    public static ExecutionResult runJavaFileDetailed(String codeFilePath) throws IOException, InterruptedException {
        File sourceFile = new File(codeFilePath);
        if (!sourceFile.isFile()) {
            return new ExecutionResult(false,
                    "[Source file not found: " + codeFilePath + "]",
                    "");
        }

        String simpleClassName = sourceFile.getName().replaceAll("\\.java$", "");
        String packageName = detectPackage(sourceFile);
        String fqcn = (packageName == null ? simpleClassName : packageName + "." + simpleClassName);

        File packageRoot = determinePackageRoot(sourceFile, packageName);
        if (packageName != null && packageRoot == null) {
            return new ExecutionResult(false,
                    "[Could not determine package root for package " + packageName + "]",
                    "");
        }
        File workDir = (packageRoot != null) ? packageRoot : sourceFile.getParentFile();

        Path workDirPath = workDir.toPath().toAbsolutePath().normalize();
        Path sourcePath = sourceFile.toPath().toAbsolutePath().normalize();
        String relativeSource = workDirPath.relativize(sourcePath).toString();

        // (1) Compile from package root so output directory tree matches package.
        ProcessBuilder compilePB = new ProcessBuilder("javac", relativeSource);
        compilePB.directory(workDir);
        Process compileProc = compilePB.start();

        String compilerMessages = readAll(compileProc.getErrorStream()) +
                                  readAll(compileProc.getInputStream());
        int compileExit = compileProc.waitFor();
        boolean compiled = compileExit == 0;

        if (!compiled) {
            return new ExecutionResult(false, compilerMessages, "");
        }

        // (2) Run with classpath = package root
        ProcessBuilder runPB = new ProcessBuilder("java", "-cp", ".", fqcn);
        runPB.directory(workDir);
        Process runProc = runPB.start();

        String stdout = readAll(runProc.getInputStream());
        String stderr = readAll(runProc.getErrorStream());
        int runExit = runProc.waitFor();

        StringBuilder programOut = new StringBuilder();
        if (!stdout.isBlank()) {
            programOut.append(stdout);
            if (!stdout.endsWith("\n")) programOut.append('\n');
        }
        if (!stderr.isBlank()) {
            programOut.append("[stderr]\n").append(stderr.trim()).append('\n');
        }
        if (runExit != 0) {
            programOut.append("[Program exited with code ").append(runExit).append("]\n");
            if (stderr.contains("Could not find or load main class")) {
                programOut.append("[Hint] FQCN: ").append(fqcn).append('\n')
                          .append("[Hint] Working dir: ").append(workDir.getAbsolutePath()).append('\n')
                          .append("[Hint] Expected: ")
                          .append(new File(workDir, fqcn.replace('.', File.separatorChar) + ".class").getAbsolutePath())
                          .append('\n');
            }
        }

        return new ExecutionResult(true, compilerMessages, programOut.toString());
    }

    /* ---------- Package / root detection ---------- */

    private static String detectPackage(File sourceFile) {
        try (BufferedReader br = new BufferedReader(new FileReader(sourceFile, StandardCharsets.UTF_8))) {
            String line; int lines = 0;
            while ((line = br.readLine()) != null && lines++ < 120) {
                Matcher m = PACKAGE_PATTERN.matcher(line);
                if (m.find()) return m.group(1);
            }
        } catch (IOException ignored) {}
        return null;
    }

    /**
     * Find directory whose trailing segments match the package path (e.g. cs_1102_base/Unit_0),
     * then strip exactly that many segments to reach the true package root.
     */
    private static File determinePackageRoot(File sourceFile, String packageName) {
        if (packageName == null) return null;
        String[] segs = packageName.split("\\.");
        Path current = sourceFile.getParentFile().toPath().toAbsolutePath();

        while (current != null) {
            if (endsWithSegments(current, segs)) {
                Path root = current;
                for (int i = 0; i < segs.length; i++) {
                    if (root != null) root = root.getParent();
                }
                return (root == null) ? null : root.toFile();
            }
            current = current.getParent();
        }
        return null;
    }

    private static boolean endsWithSegments(Path path, String[] segs) {
        int nameCount = path.getNameCount();
        if (nameCount < segs.length) return false;
        for (int i = 0; i < segs.length; i++) {
            String expect = segs[segs.length - 1 - i];
            String have = path.getName(nameCount - 1 - i).toString();
            if (!expect.equals(have)) return false;
        }
        return true;
    }

    /* ---------- Stream util ---------- */

    private static String readAll(InputStream in) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        }
    }

    /* ---------- Result record ---------- */

    public record ExecutionResult(boolean compiled,
                                  String compilerMessages,
                                  String programOutput) { }
}