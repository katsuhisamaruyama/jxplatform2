import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.Charset;

public class Test24 {

    public void m(Path directory, Charset charset) {
        try (Writer writer = new OutputStreamWriter(Files.newOutputStream(outputPath), charset)) {
            int x = inc(10);
        }
    }
    
    public int inc(int num) {
        return num + 1;
    }
    
    public OutputStreamWriter x(OutputStream s, Charset charset) {
        return new OutputStreamWriter(s, charset);
    }
    
    public int n(Path outputPath) {
        return new Files.newOutputStream(outputPath);
    }
}
