package bo.tc.tcplanner.datastructure;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class ResourceStateChange {
    //resource change
    HashMap<String, ResourceElement> resourceChange;
    //change mode
    String mode; // "delta" or "absolute"

    public ResourceStateChange() {
        resourceChange = new HashMap<>();
        mode = "absolute";
    }

    public ResourceStateChange(LinkedHashMap<String, ResourceElement> resourceChange, String mode) {
        this.resourceChange = resourceChange;
        this.mode = mode;
    }

    public HashMap<String, ResourceElement> getResourceChange() {
        return resourceChange;
    }

    public void setResourceChange(HashMap<String, ResourceElement> resourceChange) {
        this.resourceChange = resourceChange;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

}
