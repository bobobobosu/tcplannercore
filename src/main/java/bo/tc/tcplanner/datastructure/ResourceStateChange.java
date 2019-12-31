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
    //resource status
    Map<String, List<ResourceElement>> resourceStatus;
    //change mode
    String mode; // "delta" or "absolute"

    public ResourceStateChange() {
        resourceChange = new HashMap<String, List<ResourceElement>>();
        mode = "absolute";
    }

    @Override
    public ResourceStateChange removeVolatile() {
        if (resourceChange != null){
            resourceChange.values().forEach(x -> x.removeIf(y -> y.isVolatileFlag()));
            resourceChange.entrySet().removeIf(x -> x.getValue().size() == 0);
        }
        if (resourceStatus != null){
            resourceStatus.values().forEach(x -> x.removeIf(y -> y.isVolatileFlag()));
            resourceStatus.entrySet().removeIf(x -> x.getValue().size() == 0);
        }

        return this;
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

    public Map<String, List<ResourceElement>> getResourceStatus() {
        return resourceStatus;
    }

    public ResourceStateChange setResourceStatus(Map<String, List<ResourceElement>> resourceStatus) {
        this.resourceStatus = resourceStatus;
        return this;
    }
}
