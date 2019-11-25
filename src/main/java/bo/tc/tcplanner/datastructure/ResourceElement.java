package bo.tc.tcplanner.datastructure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceElement {
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

    public void setAmt(double amt) {
        this.amt = amt;
    }

    public String getRequirementLocation() {
        return requirementLocation;
    }

    public void setRequirementLocation(String requirementLocation) {
        this.requirementLocation = requirementLocation;
    }

    public String getProductionLocation() {
        return productionLocation;
    }

    public void setProductionLocation(String productionLocation) {
        this.productionLocation = productionLocation;
    }
}