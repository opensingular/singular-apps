/*
 * Copyright (C) 2016 Singular Studios (a.k.a Atom Tecnologia) - www.opensingular.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opensingular.requirement.module.workspace;

import org.opensingular.requirement.module.BoxItemDataProvider;
import org.opensingular.requirement.module.box.BoxItemDataMap;
import org.opensingular.requirement.module.box.form.STypeDynamicAdvancedFilter;
import org.opensingular.requirement.module.persistence.filter.BoxFilter;
import org.opensingular.requirement.module.service.dto.DatatableField;
import org.opensingular.requirement.module.service.dto.ItemBox;

import java.io.Serializable;
import java.util.List;

/**
 * Factory responsible for build one item box with its listings, custom actions and controllers
 */
public interface BoxDefinition extends Serializable {
    /**
     * Get the ItemBox that represents this box definition
     *
     * @return a configured ItemBox
     */
    ItemBox getItemBox();

    /**
     * @return the data provider responsible for populate the box data
     */
    BoxItemDataProvider getDataProvider();

    /**
     * @return the fields that are expected to show in the box
     */
    List<DatatableField> getDatatableFields();

    /**
     * Create a BoxFilter, this method must always return a new instance
     *
     * @return a fresh BoxFilter
     */
    default BoxFilter createBoxFilter(){
        return new BoxFilter();
    }

    /**
     * Eval if the box is visible, usually is used in conjunction
     * with {@link org.springframework.security.core.userdetails.UserDetails}
     */
    default boolean isVisible(){
        return true;
    }

    /**
     * Setup the dynamic advanced filter type, by default will add all table columns as string fields
     *
     * @param sTypeDynamicAdvancedFilter the dynamic type
     */
    default void setupDynamicAdvancedFilterType(STypeDynamicAdvancedFilter sTypeDynamicAdvancedFilter){
        for (DatatableField datatableField : getDatatableFields()) {
            sTypeDynamicAdvancedFilter.addFieldString(datatableField.getLabel())
                    .asAtr()
                    .label(datatableField.getKey())
                    .asAtrBootstrap().colPreference(2);
        }
    }

    default String getRowStyleClass(BoxItemDataMap boxItemDataMap) {
        return null;
    }
}