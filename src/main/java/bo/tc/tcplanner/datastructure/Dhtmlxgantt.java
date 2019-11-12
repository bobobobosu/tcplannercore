package bo.tc.tcplanner.datastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class Dhtmlxgantt {
    List<DhtmlxganttData> data;
    List<DhtmlxganttLink> link;
    String info;

    public List<DhtmlxganttData> getData() {
        return data;
    }

    public void setData(List<DhtmlxganttData> data) {
        this.data = data;
    }

    public List<DhtmlxganttLink> getLink() {
        return link;
    }

    public void setLink(List<DhtmlxganttLink> link) {
        this.link = link;
    }

    public String toJson() {
        try {
            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
