package formatter;

import java.io.*;
import java.nio.file.*;

public class Utils {
    public static String readFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)));
    }

    public static void writeFile(String path, String content) throws IOException {
        Files.write(Paths.get(path), content.getBytes());
    }

    public static String runJavaFile(String codeFilePath) throws IOException, InterruptedException {
        File file = new File(codeFilePath);
        String dir = file.getParent();
        String fileName = file.getName().replace(".java", "");

        // Compile
        ProcessBuilder compile = new ProcessBuilder("javac", file.getAbsolutePath());
        compile.directory(new File(dir));
        Process cproc = compile.start();
        cproc.waitFor();

        // Run
        ProcessBuilder run = new ProcessBuilder("java", fileName);
        run.directory(new File(dir));
        Process rproc = run.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(rproc.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        rproc.waitFor();

        return output.toString();
    }
}
