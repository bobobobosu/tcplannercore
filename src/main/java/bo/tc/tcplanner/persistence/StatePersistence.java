package bo.tc.tcplanner.persistence;

import bo.tc.tcplanner.app.Benchmark;
import bo.tc.tcplanner.app.TCSchedulingApp;
import bo.tc.tcplanner.domain.Schedule;
import com.google.gson.Gson;
import org.optaplanner.persistence.xstream.impl.domain.solution.XStreamSolutionFileIO;

import java.io.File;

public class StatePersistence {
    public static void persistState(Schedule schedule) {
        new XStreamSolutionFileIO<>(Schedule.class).write(schedule, new File(Benchmark.fpath_ScheduleSolution));
    }

    public static void restoreState() {
    }
}
