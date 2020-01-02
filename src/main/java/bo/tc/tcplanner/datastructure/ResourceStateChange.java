package bo.tc.tcplanner.datastructure;

import bo.tc.tcplanner.PropertyConstants;
import bo.tc.tcplanner.persistable.AbstractPersistable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

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
        this.setResourceChange(other.resourceChange.entrySet().stream().collect(
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
    public boolean checkValid() {
        checkNotNull(resourceChange);
        checkNotNull(mode);
        checkArgument(PropertyConstants.ResourceStateChangeTypes.isValid(mode));
        checkArgument(resourceChange.entrySet().stream().allMatch(x -> x.getValue().stream().allMatch(ResourceElement::checkValid)));
        return true;
    }

    @Override
    public ResourceStateChange removeVolatile() {
        resourceChange.values().forEach(x -> x.removeIf(y -> y.isVolatileFlag()));
        resourceChange.entrySet().removeIf(x -> x.getValue().size() == 0);

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ResourceStateChange that = (ResourceStateChange) o;

        if (!resourceChange.equals(that.resourceChange)) return false;
        return mode.equals(that.mode);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + resourceChange.hashCode();
        result = 31 * result + mode.hashCode();
        return result;
    }
}
