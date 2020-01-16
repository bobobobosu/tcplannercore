package bo.tc.tcplanner.datastructure;

import bo.tc.tcplanner.PropertyConstants;
import bo.tc.tcplanner.persistable.AbstractPersistable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class ResourceElementMap extends HashMap<String, List<ResourceElement>> {
    public ResourceElementMap() {
        super();
    }

    public ResourceElementMap(ResourceElementMap other) {
        other.forEach((k, v) -> this.put(k, v.stream().map(ResourceElement::new).collect(Collectors.toList())));
    }

    public boolean checkValid() {
        checkArgument(this.values().stream().allMatch(x -> x.stream().allMatch(ResourceElement::checkValid)));
        return true;
    }

    public ResourceElementMap removeVolatile() {
        this.values().forEach(x -> x.removeIf(AbstractPersistable::isVolatileFlag));
        this.entrySet().removeIf(x -> x.getValue().size() == 0);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    public ResourceElementMap removeEmpty() {
        this.forEach((k, v) -> v.removeIf(x -> x.getAmt() == 0));
        this.entrySet().removeIf(x -> x.getValue().size() == 0);
        return this;
    }
}
