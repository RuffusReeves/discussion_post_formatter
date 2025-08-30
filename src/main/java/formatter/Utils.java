package formatter;

import java.io.*;
import java.nio.file.*;

/**
 * Utility methods for file I/O operations and Java program execution.
 * 
 * This class provides essential file operations needed by the formatter
 * and handles the compilation and execution of Java source files to
 * capture program output.
 * 
 * TODO: Add comprehensive error handling with user-friendly messages
 * TODO: Implement file encoding detection and handling
 * TODO: Add support for different compilation targets and classpath management
 * TODO: Implement timeout handling for long-running programs
 */
public class Utils {
    
    /**
     * Reads the entire content of a file as a string.
     * 
     * @param path Path to the file to read
     * @return File content as string
     * @throws IOException If file cannot be read
     * 
     * TODO: Add encoding detection and specification
     * TODO: Implement file size limits for safety
     * TODO: Add progress reporting for large files
     */
    public static String readFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)));
    }

    /**
     * Writes content to a file, creating parent directories if needed.
     * 
     * @param path Path to the file to write
     * @param content Content to write
     * @throws IOException If file cannot be written
     * 
     * TODO: Add atomic write operations for safety
     * TODO: Implement backup/versioning for existing files
     * TODO: Add encoding specification options
     */
    public static void writeFile(String path, String content) throws IOException {
        Path filePath = Paths.get(path);
        
        // Create parent directories if they don't exist
        Path parentDir = filePath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }
        
        Files.write(filePath, content.getBytes());
    }

    /**
     * Compiles and runs a Java source file, capturing its output.
     * 
     * This method performs the following steps:
     * 1. Compiles the Java source file using javac
     * 2. Runs the compiled class using java
     * 3. Captures and returns the program output
     * 
     * @param codeFilePath Path to the Java source file
     * @return Program output as string
     * @throws IOException If compilation or execution fails
     * @throws InterruptedException If process is interrupted
     * 
     * TODO: Add compilation error capture and formatting
     * TODO: Implement timeout handling for infinite loops
     * TODO: Add classpath and module path support
     * TODO: Support for different Java versions and compiler options
     * TODO: Implement security sandboxing for untrusted code
     */
    public static String runJavaFile(String codeFilePath) throws IOException, InterruptedException {
        File file = new File(codeFilePath);
        String dir = file.getParent();
        String fileName = file.getName().replace(".java", "");

        // Compile the Java file
        ProcessBuilder compile = new ProcessBuilder("javac", file.getAbsolutePath());
        compile.directory(new File(dir));
        Process cproc = compile.start();
        int compileResult = cproc.waitFor();
        
        // TODO: Capture and return compilation errors if compilation fails
        if (compileResult != 0) {
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(cproc.getErrorStream()))) {
                StringBuilder errors = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errors.append(line).append("\n");
                }
                return "[Compilation failed]\n" + errors.toString();
            }
        }

        // Run the compiled class
        ProcessBuilder run = new ProcessBuilder("java", fileName);
        run.directory(new File(dir));
        Process rproc = run.start();

        // Capture output
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(rproc.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\\n");
            }
        }
        
        int runResult = rproc.waitFor();
        
        // TODO: Handle runtime errors and exit codes
        if (runResult != 0) {
            output.append("\\n[Program exited with code ").append(runResult).append("]");
        }

        return output.toString();
    }
    
    /**
     * Checks if a file exists and is readable.
     * 
     * @param path Path to check
     * @return true if file exists and is readable, false otherwise
     * 
     * TODO: Add permission checking (write, execute)
     * TODO: Implement file type validation
     */
    public static boolean fileExists(String path) {
        if (path == null || path.trim().isEmpty()) {
            return false;
        }
        
        Path filePath = Paths.get(path);
        return Files.exists(filePath) && Files.isReadable(filePath);
    }
    
    /**
     * Gets the file extension from a file path.
     * 
     * @param path File path
     * @return File extension (without dot) or empty string if no extension
     * 
     * TODO: Handle complex file naming scenarios
     */
    public static String getFileExtension(String path) {
        if (path == null || path.trim().isEmpty()) {
            return "";
        }
        
        int lastDot = path.lastIndexOf('.');
        if (lastDot > 0 && lastDot < path.length() - 1) {
            return path.substring(lastDot + 1);
        }
        
        return "";
    }
}
