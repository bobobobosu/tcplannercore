package bo.tc.tcplanner.app;

import java.io.File;
import java.io.IOException;

public class DhtmlxganttServer {
    public static void main(final String... args) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("npm.cmd", "start");
        pb.directory(new File("gantt"));
        Process p = pb.start();
        p.waitFor();
    }
}
