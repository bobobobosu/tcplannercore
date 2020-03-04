package bo.tc.tcplanner.persistence;

import bo.tc.tcplanner.domain.Schedule;
import org.optaplanner.persistence.xstream.impl.domain.solution.XStreamSolutionFileIO;

import java.io.File;

public class SolutionIO {
    public static void persistSolution(Schedule schedule) {
        XStreamSolutionFileIO<Schedule> solutionFile = new XStreamSolutionFileIO<>(Schedule.class);
        File f = new File("S:\\Desktop\\test.xml");
        solutionFile.write(schedule, f);
    }
}
