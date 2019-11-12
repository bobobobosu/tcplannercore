package bo.tc.tcplanner.datastructure.converters;

public class Schedule2Dhtmlgantt {
//    public Dhtmlxgantt convert(Schedule schedule) {
//        Dhtmlxgantt dhtmlxgantt = new Dhtmlxgantt();
//        List<DhtmlxganttData> data = new ArrayList<>();
//        List<DhtmlxganttLink> link = new ArrayList<>();
//        for (Allocation allocation : schedule.getAllocationList()) {
//            if (allocation.getJob().getJobType() == JobType.SOURCE ||
//                    allocation.getExecutionMode().getJob().getJobType() == JobType.SINK ||
//                    allocation.getExecutionMode().getJob().getName().equals("dummyJob")) continue;
//            DhtmlxganttData dataInfo = new DhtmlxganttData();
//            dataInfo.setId(allocation.getId());
//            dataInfo.setText(allocation.getJob().getName());
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//            dataInfo.setStart_date(formatter.format(allocation.getStartDate()));
//            dataInfo.setEnd_date(offset2local(allocation.getEndDate(),schedule.getGlobalStartTime()).format(formatter));
//            dataInfo.setDuration(allocation.getExecutionMode().getTimeduration());
//            data.add(dataInfo);
//        }
//        dhtmlxgantt.setData(data);
//        dhtmlxgantt.setLink(link);
//        return dhtmlxgantt;
//    }


}
