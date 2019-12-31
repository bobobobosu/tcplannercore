package bo.tc.tcplanner.datastructure;

import bo.tc.tcplanner.persistable.AbstractPersistable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ResourceStateChange extends AbstractPersistable {
    //resource change
    Map<String, List<ResourceElement>> resourceChange;
    //change mode
    String mode; // "delta" or "absolute"

    public ResourceStateChange() {
        resourceChange = new HashMap<String, List<ResourceElement>>();
        mode = "absolute";
    }

    public ResourceStateChange(ResourceStateChange other) {
        this.setResourceChange(other.resourceChange.entrySet().stream().collect(
                Collectors.toMap(
                        Map.Entry::getKey,
                        x -> x.getValue().stream().map(ResourceElement::new).collect(Collectors.toList()))));
        this.setMode(other.mode);
    }

    public ResourceStateChange(LinkedHashMap<String, List<ResourceElement>> resourceChange, String mode) {
        this.resourceChange = resourceChange;
        this.mode = mode;
    }

    public Map<String, List<ResourceElement>> getResourceChange() {
        return resourceChange;
    }

    public ResourceStateChange setResourceChange(Map<String, List<ResourceElement>> resourceChange) {
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
