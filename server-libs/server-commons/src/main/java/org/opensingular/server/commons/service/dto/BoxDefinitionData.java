package org.opensingular.server.commons.service.dto;

import java.io.Serializable;
import java.util.List;

public class BoxDefinitionData implements Serializable {

    private List<RequirementData> requirements;
    private ItemBox               itemBox;

    public BoxDefinitionData() {
    }

    public BoxDefinitionData(ItemBox itemBox, List<RequirementData> requirementsMetadata) {
        this.itemBox = itemBox;
        this.requirements = requirementsMetadata;
    }

    public List<RequirementData> getRequirements() {
        return requirements;
    }

    public void setRequirements(List<RequirementData> requirements) {
        this.requirements = requirements;
    }

    public ItemBox getItemBox() {
        return itemBox;
    }

    public void setItemBox(ItemBox itemBox) {
        this.itemBox = itemBox;
    }


}