package bo.tc.tcplanner.datastructure.persistence;

import bo.tc.tcplanner.datastructure.TimelineBlock;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.optaplanner.persistence.common.api.domain.solution.SolutionFileIO;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static bo.tc.tcplanner.app.SolverThread.initializeData;
import static bo.tc.tcplanner.app.SolverThread.initializeFiles;

public class ScheduleFileIO implements SolutionFileIO {
    private static TimelineBlock oldTimelineBlock;

    @Override
    public String getInputFileExtension() {
        return null;
    }

    @Override
    public Object read(File file) {
        try {
            oldTimelineBlock = new ObjectMapper().readValue(
                    IOUtils.toString(new FileInputStream(file), StandardCharsets.UTF_8), TimelineBlock.class);
            initializeFiles();
            return initializeData(oldTimelineBlock).getFullSchedule();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void write(Object o, File file) {
//        oldTimelineBlock = new DataStructureWriter().generateTimelineBlock(oldTimelineBlock, (Schedule) o);
//        ObjectMapper mapper = new ObjectMapper();
//        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
//        try {
//            writer.writeValue(file, oldTimelineBlock);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
