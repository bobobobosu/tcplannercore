package bo.tc.tcplanner.datastructure;

import bo.tc.tcplanner.persistable.AbstractPersistable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceElement extends AbstractPersistable {
    //numeric property
    double amt;
    //requirement availability property
    String requirementLocation;
    //production  availability property
    String productionLocation;

    public ResourceElement() {
    }

    public ResourceElement(double amt, String requirementLocation, String productionLocation) {
        this.amt = amt;
        this.requirementLocation = requirementLocation;
        this.productionLocation = productionLocation;
    }

    public ResourceElement(ResourceElement resourceElement) {
        this.amt = resourceElement.getAmt();
        this.productionLocation = resourceElement.getProductionLocation();
        this.requirementLocation = resourceElement.getRequirementLocation();
    }

    public double getAmt() {
        return amt;
    }

    public ResourceElement setAmt(double amt) {
        this.amt = amt;
        return this;
    }

    public String getRequirementLocation() {
        return requirementLocation;
    }

    public ResourceElement setRequirementLocation(String requirementLocation) {
        this.requirementLocation = requirementLocation;
        return this;
    }

    public String getProductionLocation() {
        return productionLocation;
    }

    public ResourceElement setProductionLocation(String productionLocation) {
        this.productionLocation = productionLocation;
        return this;
    }

    @Override
    public String toString() {
        return String.valueOf(amt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceElement that = (ResourceElement) o;
        return Double.compare(that.amt, amt) == 0 &&
                Objects.equals(requirementLocation, that.requirementLocation) &&
                Objects.equals(productionLocation, that.productionLocation);
    }

    @Override
    public int hashCode() {
        return 0;
    }
}