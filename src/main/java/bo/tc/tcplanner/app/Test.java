package bo.tc.tcplanner.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args) throws IOException {
        long i = (1000000 / 12) + (1000000 / 14) + (1000000 / 15) - (1000000 / (84))
                - (1000000 / 60) - (1000000 / (14 * 15)) + (1000000 / (420));

        int g = 0;
        List<Integer> few = new ArrayList<>();
        Integer de = few.get(0);
//        SolverFactory<Schedule> solverFactory;
//        TCSchedulingApp tcSchedulingApp = new TCSchedulingApp();
//        InputStream inputStream = tcSchedulingApp.getClass().getResourceAsStream("/solverPhase1.xml");
//        String s = IOUtils.toString(inputStream);
//        InputStream inputStream1 = new ByteArrayInputStream(s.getBytes(Charset.forName("UTF-8")));
//        solverFactory = SolverFactory.createFromXmlInputStream(inputStream1);
//        Solver solver1 = solverFactory.buildSolver();
//        int h=0;
    }
}
