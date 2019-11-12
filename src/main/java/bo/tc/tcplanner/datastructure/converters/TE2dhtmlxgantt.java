package bo.tc.tcplanner.datastructure.converters;

import bo.tc.tcplanner.datastructure.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TE2dhtmlxgantt {
    public Dhtmlxgantt convert(TimelineBlock timelineBlock) {
        Dhtmlxgantt GanttData = new Dhtmlxgantt();
        List<DhtmlxganttData> data = new ArrayList<>();
        List<DhtmlxganttLink> link = new ArrayList<>();
        for (TimelineEntry TE : timelineBlock.getTimelineEntryList()) {
            DhtmlxganttData dataInfo = new DhtmlxganttData();
            dataInfo.setId(TE.getId());
            dataInfo.setText(TE.getTitle());
            dataInfo.setDescription(TE.getDescription());
            dataInfo.setProgress((int) (100
                    * (TE.getProgressChange().getPlannedEndPercent() - TE.getProgressChange().getStartPercent())));
            dataInfo.setStart_date(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(ZonedDateTime.parse(TE.getStartTime())));
            dataInfo.setEnd_date(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(
                    ZonedDateTime.parse(TE.getStartTime()).plusMinutes((long) TE.getHumanStateChange().getDuration())));
            dataInfo.setDuration(Double.valueOf(TE.getHumanStateChange().getDuration()).intValue());
            data.add(dataInfo);
        }
        GanttData.setInfo(timelineBlock.getScore());
        GanttData.setData(data);
        GanttData.setLink(link);
        return GanttData;
    }

    public void applydhtmlxganttToTE(HashMap<String, Object> dhtmlxgantt, TimelineBlock TimelineBlock) {

    }

}
