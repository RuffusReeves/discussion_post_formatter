package formatter;

import java.io.*;
import java.nio.file.*;

/**
 * Utility methods for file I/O operations and Java program execution.
 *
 * New:
 *  - Added ExecutionResult record
 *  - Added runJavaFileDetailed(...) which captures compiler messages (stderr + stdout)
 *    separately from program runtime output.
 *  - Original runJavaFile(...) kept for backward compatibility (now delegates).
 */
public class Utils {

    /**
     * Reads the entire content of a file as a string.
     */
    public static String readFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)));
    }

    /**
     * Writes content to a file, creating parent directories if needed.
     */
    public static void writeFile(String path, String content) throws IOException {
        Path filePath = Paths.get(path);
        Path parentDir = filePath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }
        Files.write(filePath, content.getBytes());
    }

    /**
     * Backward compatible helper: returns ONLY program output (or compilation failure output).
     * Internally uses the new detailed method.
     */
    public static String runJavaFile(String codeFilePath) throws IOException, InterruptedException {
        ExecutionResult res = runJavaFileDetailed(codeFilePath);
        if (!res.compiled()) {
            // If compilation failed, return compiler messages prefixed
            return "[Compilation failed]\n" + res.compilerMessages();
        }
        return res.programOutput();
    }

    /**
     * Detailed execution result capturing compilation diagnostics AND program output.
     *
     * compilerMessages:
     *   - All lines emitted by javac to stderr (and stdout, rarely used by javac)
     *   - Empty String if no diagnostics
     *
     * programOutput:
     *   - Stdout of the executed class (if compilation succeeded)
     *   - Empty String if compilation failed
     *
     * compiled: true if javac exit code == 0
     *
     * Notes:
     *  - We do NOT persist compiler messages; caller can decide how to present them.
     *  - No timeout / sandboxing (future improvement).
     */
    public static ExecutionResult runJavaFileDetailed(String codeFilePath) throws IOException, InterruptedException {
        File file = new File(codeFilePath);
        if (!file.isFile()) {
            return new ExecutionResult(false, "[Source file not found: " + codeFilePath + "]", "");
        }
        String dir = file.getParent();
        String className = file.getName().replace(".java", "");

        // 1. Compile
        ProcessBuilder compilePB = new ProcessBuilder("javac", file.getAbsolutePath());
        compilePB.directory(new File(dir));
        Process compileProc = compilePB.start();

        StringBuilder compilerMsgs = new StringBuilder();
        // Capture stderr
        try (BufferedReader err = new BufferedReader(new InputStreamReader(compileProc.getErrorStream()))) {
            String line;
            while ((line = err.readLine()) != null) {
                compilerMsgs.append(line).append('\n');
            }
        }
        // Capture stdout from compiler (rare, but include it)
        try (BufferedReader out = new BufferedReader(new InputStreamReader(compileProc.getInputStream()))) {
            String line;
            while ((line = out.readLine()) != null) {
                compilerMsgs.append(line).append('\n');
            }
        }

        int compileExit = compileProc.waitFor();
        boolean compiled = compileExit == 0;

        if (!compiled) {
            return new ExecutionResult(false, compilerMsgs.toString(), "");
        }

        // 2. Run
        ProcessBuilder runPB = new ProcessBuilder("java", className);
        runPB.directory(new File(dir));
        Process runProc = runPB.start();

        StringBuilder programOut = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(runProc.getInputStream()))) {
            String line;
            while ((line = r.readLine()) != null) {
                programOut.append(line).append('\n');
            }
        }
        // (Optionally capture stderr of program - could append or separate. For now ignore or could add.)
        int runExit = runProc.waitFor();
        if (runExit != 0) {
            programOut.append("\n[Program exited with code ").append(runExit).append("]");
        }

        return new ExecutionResult(true, compilerMsgs.toString(), programOut.toString());
    }

    /**
     * Simple existence + readability check.
     */
    public static boolean fileExists(String path) {
        if (path == null || path.trim().isEmpty()) return false;
        Path filePath = Paths.get(path);
        return Files.exists(filePath) && Files.isReadable(filePath);
    }

    /**
     * Extract file extension.
     */
    public static String getFileExtension(String path) {
        if (path == null || path.trim().isEmpty()) return "";
        int lastDot = path.lastIndexOf('.');
        if (lastDot > 0 && lastDot < path.length() - 1) {
            return path.substring(lastDot + 1);
        }
        return "";
    }

    /**
     * Result container for detailed compilation + execution.
     */
    public record ExecutionResult(boolean compiled,
                                  String compilerMessages,
                                  String programOutput) { }
}