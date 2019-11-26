package bo.tc.tcplanner.datastructure;

import bo.tc.tcplanner.persistable.AbstractPersistable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ResourceStateChange extends AbstractPersistable {
    //resource change
    Map<String, ResourceElement> resourceChange;
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

    public Map<String, ResourceElement> getResourceChange() {
        return resourceChange;
    }

    public ResourceStateChange setResourceChange(Map<String, ResourceElement> resourceChange) {
        this.resourceChange = resourceChange;
        return this;
    }

    public String getMode() {
        return mode;
    }

    public ResourceStateChange setMode(String mode) {
        this.mode = mode;
        return this;
    }

}
