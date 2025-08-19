package discussion_formatter;

import java.io.*;
import java.nio.file.*;

public class Utils {
    public static String readFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)));
    }
    public static void writeFile(String path, String content) throws IOException {
        Path p = Paths.get(path);
        if (p.getParent()!=null) Files.createDirectories(p.getParent());
        Files.write(p, content.getBytes());
    }
    public static String runJavaFile(String codeFilePath) throws IOException, InterruptedException {
        File file = new File(codeFilePath);
        String dir = file.getParent();
        String fileName = file.getName().replace(".java","");
        ProcessBuilder compile = new ProcessBuilder("javac", file.getAbsolutePath());
        compile.directory(new File(dir));
        compile.redirectErrorStream(true);
        Process c = compile.start();
        String cOut = new String(c.getInputStream().readAllBytes());
        if (c.waitFor()!=0) return "[Compilation failed]\\n"+cOut;
        ProcessBuilder run = new ProcessBuilder("java", fileName);
        run.directory(new File(dir));
        run.redirectErrorStream(true);
        Process r = run.start();
        String rOut = new String(r.getInputStream().readAllBytes());
        r.waitFor();
        return rOut;
    }
}
