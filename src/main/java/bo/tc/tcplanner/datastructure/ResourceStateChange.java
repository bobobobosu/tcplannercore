package bo.tc.tcplanner.datastructure;

import bo.tc.tcplanner.persistable.AbstractPersistable;

import java.util.*;
import java.util.stream.Collectors;

public class ResourceStateChange extends AbstractPersistable {
    //resource change
    Map<String, List<ResourceElement>> resourceChange;
    //resource status
    Map<String, List<ResourceElement>> resourceStatus;
    //change mode
    String mode; // "delta" or "absolute"

    public ResourceStateChange() {
        super();
    }

    public ResourceStateChange(ResourceStateChange other) {
        super(other);
        if (other.getResourceChange() != null) this.setResourceChange(other.resourceChange.entrySet().stream().collect(
                Collectors.toMap(
                        Map.Entry::getKey,
                        x -> x.getValue().stream().map(ResourceElement::new).collect(Collectors.toList()))));
        if (other.getResourceStatus() != null) this.setResourceStatus(other.resourceStatus.entrySet().stream().collect(
                Collectors.toMap(
                        Map.Entry::getKey,
                        x -> x.getValue().stream().map(ResourceElement::new).collect(Collectors.toList()))));
        this.setMode(other.mode);
    }

    @Override
    public ResourceStateChange removeVolatile() {
        if (resourceChange != null) {
            resourceChange.values().forEach(x -> x.removeIf(y -> y.isVolatileFlag()));
            resourceChange.entrySet().removeIf(x -> x.getValue().size() == 0);
        }
        if (resourceStatus != null) {
            resourceStatus.values().forEach(x -> x.removeIf(y -> y.isVolatileFlag()));
            resourceStatus.entrySet().removeIf(x -> x.getValue().size() == 0);
        }

        return this;
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

    public ResourceStateChange removeEmpty() {
        if (!(resourceStatus == null)) resourceStatus.forEach((k, v) -> v.removeIf(x -> x.getAmt() == 0));
        if (!(resourceStatus == null)) resourceStatus.entrySet().removeIf(x -> x.getValue().size() == 0);
        if (!(resourceChange == null)) resourceChange.forEach((k, v) -> v.removeIf(x -> x.getAmt() == 0));
        if (!(resourceChange == null)) resourceChange.entrySet().removeIf(x -> x.getValue().size() == 0);
        return this;
    }

    public ResourceStateChange addResourceElementToChange(String key, ResourceElement resourceElement) {
        if (resourceChange == null) resourceChange = new TreeMap<>();
        if (!resourceChange.containsKey(key)) resourceChange.put(key, new ArrayList<>());
        resourceChange.get(key).add(resourceElement);
        return this;
    }

    public ResourceStateChange addResourceElementToStatus(String key, ResourceElement resourceElement) {
        if (resourceStatus == null) resourceStatus = new TreeMap<>();
        if (!resourceStatus.containsKey(key)) resourceStatus.put(key, new ArrayList<>());
        resourceStatus.get(key).add(resourceElement);
        return this;
    }
}
